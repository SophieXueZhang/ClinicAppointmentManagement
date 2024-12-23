import os
import json
import base64
import tempfile
import unittest
from pathlib import Path
import sys

# Add the parent directory to Python path to import our module
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from qr_transfer import (
    convert_file_to_qr_codes,
    process_images_in_directory,
    save_intermediate_state,
    load_intermediate_state
)

class TestQRTransfer(unittest.TestCase):
    def setUp(self):
        # 创建临时目录用于测试
        self.test_dir = tempfile.mkdtemp()
        self.qr_codes_dir = os.path.join(self.test_dir, 'qr_codes')
        self.output_dir = os.path.join(self.test_dir, 'output')
        os.makedirs(self.qr_codes_dir, exist_ok=True)
        os.makedirs(self.output_dir, exist_ok=True)
        
        # 创建测试文件
        self.test_file = os.path.join(self.test_dir, 'test.txt')
        with open(self.test_file, 'w') as f:
            f.write('This is a test file content for QR code transfer testing.')
    
    def test_qr_code_generation(self):
        """测试QR码生成功能，包括JSON格式和序列信息"""
        output_file = os.path.join(self.output_dir, 'output.txt')
        
        # 生成全部QR码
        convert_file_to_qr_codes(self.test_file, self.qr_codes_dir, batch_size=10)
        
        # 验证生成的QR码
        qr_files = os.listdir(self.qr_codes_dir)
        self.assertTrue(len(qr_files) > 0, "应该生成至少一个QR码")
        
        # 处理QR码并验证结果
        process_images_in_directory(self.qr_codes_dir, output_file)
        
        # 验证输出文件内容
        with open(output_file, 'r') as f:
            content = f.read()
        self.assertEqual(content, 'This is a test file content for QR code transfer testing.')
    
    def test_selective_generation(self):
        """测试选择性生成QR码功能"""
        # 首先生成所有QR码以获取总数
        convert_file_to_qr_codes(self.test_file, self.qr_codes_dir, batch_size=10)
        total_qrs = len(os.listdir(self.qr_codes_dir))
        
        # 清空目录
        for file in os.listdir(self.qr_codes_dir):
            os.remove(os.path.join(self.qr_codes_dir, file))
        
        # 只生成部分QR码
        selected_indexes = [0, 2]  # 只生成第1和第3个QR码
        convert_file_to_qr_codes(self.test_file, self.qr_codes_dir, batch_size=10, selected_indexes=selected_indexes)
        
        # 验证只生成了选定的QR码
        generated_qrs = os.listdir(self.qr_codes_dir)
        self.assertEqual(len(generated_qrs), len(selected_indexes))
    
    def test_intermediate_state(self):
        """测试中间状态保存和恢复功能"""
        output_file = os.path.join(self.output_dir, 'output.txt')
        intermediate_file = os.path.join(self.output_dir, 'intermediate.json')
        
        # 生成部分QR码
        convert_file_to_qr_codes(self.test_file, self.qr_codes_dir, batch_size=10)
        qr_files = os.listdir(self.qr_codes_dir)
        
        # 只处理一半的QR码
        half_qrs = qr_files[:len(qr_files)//2]
        for file in qr_files[len(qr_files)//2:]:
            os.remove(os.path.join(self.qr_codes_dir, file))
        
        # 处理部分QR码，应该生成中间状态
        missing_sequences = process_images_in_directory(self.qr_codes_dir, output_file, intermediate_file)
        
        # 验证中间状态文件存在
        self.assertTrue(os.path.exists(intermediate_file))
        
        # 验证返回了缺失的序号
        self.assertTrue(isinstance(missing_sequences, list))
        self.assertTrue(len(missing_sequences) > 0)
    
    def test_resume_from_intermediate(self):
        """测试从中间状态恢复并完成传输"""
        output_file = os.path.join(self.output_dir, 'output.txt')
        intermediate_file = os.path.join(self.output_dir, 'intermediate.json')
        
        # 首先生成所有QR码
        convert_file_to_qr_codes(self.test_file, self.qr_codes_dir, batch_size=10)
        all_qr_files = os.listdir(self.qr_codes_dir)
        
        # 第一次只处理一半的QR码
        half_point = len(all_qr_files) // 2
        for file in all_qr_files[half_point:]:
            os.remove(os.path.join(self.qr_codes_dir, file))
        
        # 第一次处理，获取缺失的序号
        missing_sequences = process_images_in_directory(self.qr_codes_dir, output_file, intermediate_file)
        
        # 清空QR码目录
        for file in os.listdir(self.qr_codes_dir):
            os.remove(os.path.join(self.qr_codes_dir, file))
        
        # 只生成缺失的QR码
        convert_file_to_qr_codes(self.test_file, self.qr_codes_dir, batch_size=10, selected_indexes=missing_sequences)
        
        # 从中间状态继续处理
        final_missing = process_images_in_directory(self.qr_codes_dir, output_file, intermediate_file)
        
        # 验证所有数据都已完整接收
        self.assertIsNone(final_missing)
        
        # 验证最终文件内容
        with open(output_file, 'r') as f:
            content = f.read()
        self.assertEqual(content, 'This is a test file content for QR code transfer testing.')
    
    def tearDown(self):
        # 清理测试目录
        import shutil
        shutil.rmtree(self.test_dir)

if __name__ == '__main__':
    unittest.main()
