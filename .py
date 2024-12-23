#!/usr/bin/env python
# coding: utf-8

# # 将文件切分并生成二维码

# In[ ]:


pip install numpy==1.24.3 opencv-contrib-python==4.5.2.54 opencv-python==4.5.2.54 tqdm==4.65.0


# In[ ]:





# In[ ]:





# In[2]:


import os
import qrcode
from PIL import Image
import base64
import cv2
from tqdm.notebook import tqdm

import json

def split_bytes_into_batches(file_bytes, batch_size):
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
    # data已经是JSON字符串的bytes形式，直接解码即可
    json_str = data.decode('utf-8')

    qr = qrcode.QRCode(
        version=None,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(json_str)
    qr.make(fit=True)
    qr_img = qr.make_image(fill_color="black", back_color="white")
    qr_img.save(output_path)
    
def convert_file_to_qr_codes(file_path, output_directory, batch_size, selected_indexes=None):
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

    
    
# 二维码合成视频


def images_to_video(image_directory, output_path):
    image_files = sorted([f for f in os.listdir(image_directory) if f.endswith('.jpg') or f.endswith('.png')])

    if len(image_files) == 0:
        print("该目录中没有找到图片文件！")
        return

    # 获取图片总数量
    image_count = len(image_files)

    # 获取最大图片尺寸
    max_width = 0
    max_height = 0
    for image_file in image_files:
        image_path = os.path.join(image_directory, image_file)
        image = cv2.imread(image_path)
        height, width, _ = image.shape
        max_width = max(max_width, width)
        max_height = max(max_height, height)

    # 计算视频长度和帧率
    video_length = image_count
    fps = 12

    # 创建视频编码器
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')  # 可根据需要更改编码器
    video = cv2.VideoWriter(output_path, fourcc, fps, (max_width, max_height))
    counter = 0

    # 将每张图片逐帧写入视频

    counter = 0
    for image_file in tqdm(image_files, total=len(image_files), desc="Processing images"):
        image_path = os.path.join(image_directory, image_file)
        frame = cv2.imread(image_path)

        # 调整图片尺寸以使其与最大尺寸一致
        frame_height, frame_width, _ = frame.shape
        if frame_height != max_height or frame_width != max_width:
            frame = cv2.resize(frame, (max_width, max_height))

        video.write(frame)
        counter += 1

    # 释放视频编码器资源
    video.release()

    print("视频转换完成！")
    print(counter)
    

# 清空中间文件

def delete_all_files_in_directory(directory):
    for filename in os.listdir(directory):
        file_path = os.path.join(directory, filename)
        if os.path.isfile(file_path):
            os.remove(file_path)


# In[3]:


# 每个二维码批次的字符数
batch_size = 500  
# 输入的文件路径
file_path = '/Users/huanghaozhou/Downloads/profile(4).txt.zip' 
# 输出视频的路径
output_path = '/Users/huanghaozhou/Downloads/scan_to_all_file/profile(4).mp4'  
# 中间过程
output_directory = os.path.dirname(file_path)+'/qr_code'
image_directory = output_directory


delete_all_files_in_directory(output_directory)
convert_file_to_qr_codes(file_path, output_directory, batch_size)
images_to_video(image_directory, output_path)
delete_all_files_in_directory(output_directory)


# # 将手机录制的视频切分成图像（Resize 并做灰度处理）

# In[2]:


import cv2
import os

def split_video_to_frames(video_path, output_directory, output_resolution=(640, 480)):
    # 创建输出目录
    os.makedirs(output_directory, exist_ok=True)

    # 打开视频文件
    video = cv2.VideoCapture(video_path)

    frame_count = 0

    while video.isOpened():
        ret, frame = video.read()

        if not ret:
            break

        # 将帧转换为灰度图像
        gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # 调整图像分辨率
        resized_frame = cv2.resize(gray_frame, output_resolution)

        # 生成输出图像文件名
        output_file = os.path.join(output_directory, f'frame_{str(frame_count+100000)}.png')

        # 保存当前帧为图像文件
        cv2.imwrite(output_file, resized_frame)

        frame_count += 1

    # 关闭视频文件
    video.release()

    print(f"成功将视频切分为{frame_count}帧，并保存在{output_directory}目录中。")


# # 扫码并拼合文件

# In[5]:


import cv2
import os
from tqdm.notebook import tqdm
import base64
detector = cv2.wechat_qrcode_WeChatQRCode()

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
    files = os.listdir(directory)
    files.sort()
    
    # 从中间文件恢复状态（如果存在）
    scanned_data = {}
    max_sequence_id = -1
    if intermediate_file and os.path.exists(intermediate_file):
        scanned_data, max_sequence_id = load_intermediate_state(intermediate_file)
    
    pbar = tqdm(total=len(files), desc="扫描二维码")
    counter = len(scanned_data)

    for file in files:
        pbar.update(1)
        if file.endswith('.jpg') or file.endswith('.jpeg') or file.endswith('.png'):
            file_path = os.path.join(directory, file)
            image = cv2.imread(file_path)
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            decoded_objects, points = detector.detectAndDecode(gray)

            if len(decoded_objects) > 0:
                try:
                    # 解析JSON数据
                    json_data = json.loads(decoded_objects[0])
                    sequence_id = json_data['sequence_id']
                    max_sequence_id = max(max_sequence_id, json_data['max_sequence_id'])
                    chunk_data = base64.b64decode(json_data['data'])

                    # 如果这个序号的数据还没有被扫描过，则添加到字典中
                    if sequence_id not in scanned_data:
                        scanned_data[sequence_id] = chunk_data
                        counter += 1
                        # 每成功扫描一个新的数据块就保存中间状态
                        save_intermediate_state(scanned_data, max_sequence_id, 
                                             intermediate_file if intermediate_file else output_file + '.intermediate.json')
                except (json.JSONDecodeError, KeyError, base64.binascii.Error) as e:
                    print(f"警告：处理文件 {file} 时出错：{str(e)}")
                    continue

    def check_completeness(scanned_data_dict, max_seq_id):
        """检查数据完整性并返回缺失的序号列表"""
        all_sequence_ids = set(scanned_data_dict.keys())
        expected_sequence_ids = set(range(max_seq_id + 1))
        missing_sequences = sorted(list(expected_sequence_ids - all_sequence_ids))
        return len(missing_sequences) == 0, missing_sequences

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




# In[7]:


# 使用示例
video_path = '/Users/huanghaozhou/Downloads/IMG_7447.mp4'  # 输入的视频文件路径
output_directory = os.path.dirname(video_path)+'/output_qr_code'  # 输出图像帧的目录
output_resolution = (1280, 720) 
directory = output_directory  
output_file = '/Users/huanghaozhou/Downloads/profile_remake1.txt'  # 输出扫码结果的文件路径

split_video_to_frames(video_path, output_directory, output_resolution)
process_images_in_directory(directory, output_file)


# In[ ]:





# In[ ]:





# # 下方为代码备份

# # 将手机录制的视频切分成图像（不做灰度处理）

# In[1]:


import cv2
import os

def split_video_to_frames(video_path, output_directory):
    # 创建输出目录
    os.makedirs(output_directory, exist_ok=True)

    # 打开视频文件
    video = cv2.VideoCapture(video_path)

    frame_count = 0

    while video.isOpened():
        ret, frame = video.read()

        if not ret:
            break

        # 生成输出图像文件名
        output_file = os.path.join(output_directory, f'frame_{str(frame_count+100000)}.png')

        # 保存当前帧为图像文件
        cv2.imwrite(output_file, frame)

        frame_count += 1

    # 关闭视频文件
    video.release()

    print(f"成功将视频切分为{frame_count}帧，并保存在{output_directory}目录中。")

# 使用示例
video_path = '/Users/huanghaozhou/Downloads/scan_to_qrcode/CAM_6.MP4'  # 输入的视频文件路径
output_directory = '/Users/huanghaozhou/Downloads/scan_to_qrcode/zip_r_code_6'  # 输出图像帧的目录

split_video_to_frames(video_path, output_directory)


# In[ ]:




