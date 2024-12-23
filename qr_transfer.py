import os
import json
import base64
import qrcode
import cv2
import numpy as np
from tqdm import tqdm
from pyzbar.pyzbar import decode

def split_bytes_into_batches(file_bytes, batch_size):
    """将文件数据分割成小块"""
    total_chunks = len(file_bytes) // batch_size + (1 if len(file_bytes) % batch_size else 0)
    batches = []
    for i in range(total_chunks):
        chunk_data = file_bytes[i*batch_size:(i+1)*batch_size]
        chunk_obj = {
            "sequence_id": i,
            "max_sequence_id": total_chunks - 1,
            "data": base64.b64encode(chunk_data).decode('utf-8')
        }
        batches.append(json.dumps(chunk_obj).encode('utf-8'))
    return batches

def generate_qr_code(data, output_path):
    """生成QR码"""
    qr = qrcode.QRCode(version=None, error_correction=qrcode.constants.ERROR_CORRECT_L)
    qr.add_data(data)
    qr.make(fit=True)
    qr_img = qr.make_image(fill_color="black", back_color="white")
    qr_img.save(output_path)

def convert_file_to_qr_codes(file_path, output_directory, batch_size, selected_indexes=None):
    """将文件转换为QR码序列"""
    with open(file_path, 'rb') as file:
        file_bytes = file.read()

    batches = split_bytes_into_batches(file_bytes, batch_size)

    if not os.path.exists(output_directory):
        os.makedirs(output_directory)
    
    # 如果没有指定序号，则生成所有二维码
    if not selected_indexes:
        indexes_to_generate = range(len(batches))
    else:
        # 验证选定的序号是否有效
        max_index = len(batches) - 1
        indexes_to_generate = [i for i in selected_indexes if 0 <= i <= max_index]
        if len(indexes_to_generate) != len(selected_indexes):
            invalid_indexes = [i for i in selected_indexes if i > max_index or i < 0]
            print(f"警告：以下序号无效，将被忽略：{invalid_indexes}")
    
    for i in tqdm(indexes_to_generate, total=len(indexes_to_generate), desc="生成二维码中"):
        output_path = os.path.join(output_directory, f'qr_code_{str(i+100000)}.png')
        generate_qr_code(batches[i], output_path)

    print("转换完成！")

def save_intermediate_state(scanned_data, max_sequence_id, save_file):
    """保存中间状态到文件"""
    try:
        intermediate_state = {
            'max_sequence_id': max_sequence_id,
            'scanned_chunks': {str(k): base64.b64encode(v).decode('utf-8') 
                           for k, v in scanned_data.items()}
        }
        with open(save_file, 'w') as f:
            json.dump(intermediate_state, f)
    except Exception as e:
        print(f"警告：保存中间状态时出错：{str(e)}")

def load_intermediate_state(intermediate_file):
    """从中间文件加载状态"""
    try:
        with open(intermediate_file, 'r') as f:
            intermediate_state = json.load(f)
            max_sequence_id = intermediate_state['max_sequence_id']
            scanned_data = {}
            for seq_id_str, b64_data in intermediate_state['scanned_chunks'].items():
                seq_id = int(seq_id_str)
                scanned_data[seq_id] = base64.b64decode(b64_data)
        print(f"已从中间文件恢复 {len(scanned_data)} 个数据块")
        return scanned_data, max_sequence_id
    except Exception as e:
        print(f"警告：读取中间文件时出错：{str(e)}")
        return {}, -1


def process_images_in_directory(directory, output_file, intermediate_file=None):
    """处理目录中的QR码图片"""
    files = os.listdir(directory)
    files.sort()
    
    # 从中间文件恢复状态（如果存在）
    scanned_data = {}
    max_sequence_id = -1
    if intermediate_file and os.path.exists(intermediate_file):
        scanned_data, max_sequence_id = load_intermediate_state(intermediate_file)
    
    pbar = tqdm(total=len(files), desc="扫描二维码")
    counter = len(scanned_data)

    def check_completeness(scanned_data_dict, max_seq_id):
        """检查数据完整性并返回缺失的序号列表"""
        all_sequence_ids = set(scanned_data_dict.keys())
        expected_sequence_ids = set(range(max_seq_id + 1))
        missing_sequences = sorted(list(expected_sequence_ids - all_sequence_ids))
        return len(missing_sequences) == 0, missing_sequences

    for file in files:
        file_path = os.path.join(directory, file)
        try:
            # 读取图片
            img = cv2.imread(file_path)
            if img is None:
                print(f"警告：无法读取文件 {file}")
                continue
            
            # 解码QR码
            decoded_objects = decode(img)
            for obj in decoded_objects:
                try:
                    # 解析JSON数据
                    chunk_info = json.loads(obj.data.decode('utf-8'))
                    sequence_id = chunk_info['sequence_id']
                    max_sequence_id = max(max_sequence_id, chunk_info['max_sequence_id'])
                    chunk_data = base64.b64decode(chunk_info['data'])
                    
                    if sequence_id not in scanned_data:
                        scanned_data[sequence_id] = chunk_data
                        counter += 1
                        # 每成功扫描一个新的数据块就保存中间状态
                        save_intermediate_state(scanned_data, max_sequence_id, 
                                             intermediate_file if intermediate_file else output_file + '.intermediate.json')
                except (json.JSONDecodeError, KeyError, base64.binascii.Error) as e:
                    print(f"警告：处理文件 {file} 时出错：{str(e)}")
                    continue
        except Exception as e:
            print(f"警告：处理文件 {file} 时出错：{str(e)}")
            continue
        finally:
            pbar.update(1)
    
    pbar.close()
    print(f"扫描完成，共处理 {counter} 个数据块")
    
    # 检查数据完整性并获取缺失的序号
    is_complete, missing_sequences = check_completeness(scanned_data, max_sequence_id)
    
    if is_complete:
        try:
            # 按序号顺序合并数据
            merged_data = b''.join(scanned_data[i] for i in range(max_sequence_id + 1))
            # 将合并后的数据写入输出文件
            with open(output_file, 'wb') as f:
                f.write(merged_data)
            print(f"处理完成！扫码结果已保存在 {output_file} 文件中。")
            print(f"共处理 {counter} 条数据")
            return None  # 返回None表示处理完成
        except Exception as e:
            print(f"错误：合并数据时出错：{str(e)}")
            missing_sequences = list(range(max_sequence_id + 1))  # 如果出错，认为所有数据都需要重新生成
    else:
        print(f"警告：数据不完整，缺少以下序号的数据：{missing_sequences}")
        # 保存中间状态
        save_file = intermediate_file if intermediate_file else output_file + '.intermediate.json'
        save_intermediate_state(scanned_data, max_sequence_id, save_file)
        print(f"中间状态已保存到：{save_file}")
        return missing_sequences
