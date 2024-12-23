import nbformat as nbf
import json

# Create a new notebook
nb = nbf.v4.new_notebook()

# Title and introduction
markdown_intro = """# QR码文件传输系统演示

本notebook演示了一个基于QR码的文件传输系统，具有以下特点：

1. 文件分块与JSON元数据
2. 选择性QR码生成
3. 中间状态保存和恢复
4. 缺失块检测和报告

让我们开始逐步了解系统的功能。"""

# Imports cell
code_imports = '''import os
import json
import base64
import qrcode
import cv2
import numpy as np
from tqdm import tqdm
from pyzbar.pyzbar import decode

# 导入我们的QR码传输模块
from qr_transfer import (
    split_bytes_into_batches,
    generate_qr_code,
    convert_file_to_qr_codes,
    save_intermediate_state,
    load_intermediate_state,
    process_images_in_directory
)'''

# Test file preparation
markdown_missing_input = """## 缺失输入处理

系统能够优雅地处理各种输入错误情况：
- 文件不存在
- 权限不足
- 文件损坏
- 目录不可访问
- 磁盘空间不足

下面演示如何处理这些错误情况。"""

code_missing_input = '''# 演示各种输入错误处理
import os
import shutil
from pathlib import Path

def test_input_scenarios():
    """测试各种输入错误场景"""
    scenarios = [
        ("不存在的文件", "nonexistent.txt", "qr_codes"),
        ("权限受限的目录", "test.txt", "/root/restricted"),
        ("磁盘空间不足", "test.txt", "no_space"),
        ("损坏的输入文件", "corrupted.txt", "qr_codes")
    ]
    
    # 创建测试文件
    with open("test.txt", "w") as f:
        f.write("测试内容")
    
    # 创建损坏的文件
    with open("corrupted.txt", "wb") as f:
        f.write(b"\\xFF\\xFF\\xFF")  # 无效的UTF-8数据
    
    for scenario_name, input_file, output_dir in scenarios:
        print(f"\\n测试场景：{scenario_name}")
        try:
            # 尝试处理输入
            print(f"处理文件：{input_file}")
            print(f"输出目录：{output_dir}")
            
            # 验证输入文件
            if not os.path.exists(input_file):
                raise FileNotFoundError(f"输入文件不存在：{input_file}")
            
            # 验证输出目录权限
            try:
                os.makedirs(output_dir, exist_ok=True)
            except PermissionError:
                raise PermissionError(f"无权限创建目录：{output_dir}")
            
            # 检查磁盘空间
            if output_dir == "no_space":
                raise OSError("磁盘空间不足")
            
            # 尝试读取文件
            with open(input_file, "r") as f:
                content = f.read()
                print(f"成功读取文件内容：{content[:50]}...")
                
        except Exception as e:
            print(f"错误：{str(e)}")
            print("建议解决方案：")
            if isinstance(e, FileNotFoundError):
                print("- 检查文件路径是否正确")
                print("- 确认文件名大小写")
                print("- 验证文件是否被移动或删除")
            elif isinstance(e, PermissionError):
                print("- 检查文件/目录权限")
                print("- 使用适当的用户权限运行程序")
            elif isinstance(e, OSError):
                print("- 清理磁盘空间")
                print("- 选择其他输出位置")
            else:
                print("- 检查文件编码")
                print("- 确保文件未被损坏")
        
        print("\\n---")

    # 清理测试文件
    os.remove("test.txt")
    os.remove("corrupted.txt")

# 运行测试场景
test_input_scenarios()'''

markdown_path_handling = """## 输入输出路径处理

在使用QR码传输系统之前，我们需要了解如何正确处理输入和输出路径。系统支持以下路径处理功能：
- 相对路径和绝对路径
- 自动创建输出目录
- 路径验证和规范化
- 特殊字符处理

让我们通过示例来了解这些功能。"""

code_path_handling = '''# 演示路径处理
import os
from pathlib import Path

def setup_paths(input_path, output_dir):
    """设置并验证输入输出路径"""
    # 转换为绝对路径
    input_path = os.path.abspath(input_path)
    output_dir = os.path.abspath(output_dir)
    
    # 验证输入文件
    if not os.path.exists(input_path):
        raise FileNotFoundError(f"输入文件不存在：{input_path}")
    
    # 创建输出目录
    os.makedirs(output_dir, exist_ok=True)
    
    return input_path, output_dir

# 演示不同的路径格式
paths_to_try = [
    ("./test_file.txt", "./qr_codes"),  # 相对路径
    (os.path.abspath("test_file.txt"), os.path.abspath("qr_codes")),  # 绝对路径
    ("test_file.txt", "qr_codes/subfolder"),  # 子目录
]

for input_path, output_dir in paths_to_try:
    try:
        print(f"\\n处理路径：")
        print(f"输入：{input_path}")
        print(f"输出：{output_dir}")
        
        normalized_input, normalized_output = setup_paths(input_path, output_dir)
        
        print("规范化后的路径：")
        print(f"输入：{normalized_input}")
        print(f"输出：{normalized_output}")
        
    except Exception as e:
        print(f"错误：{str(e)}")'''

markdown_test_prep = """## 准备测试文件

首先，我们创建一个简单的测试文件来演示系统功能。"""

code_test_prep = '''# 创建测试文件
test_content = "这是一个测试文件，用于演示QR码文件传输系统的功能。" * 10
test_file = "test_file.txt"

with open(test_file, "w", encoding="utf-8") as f:
    f.write(test_content)

print(f"已创建测试文件：{test_file}")
print(f"文件大小：{os.path.getsize(test_file)} 字节")'''

# JSON format demo
markdown_json = """## 1. 文件分块与JSON元数据

系统将文件分割成小块，每个块都包含以下JSON格式的元数据：
- sequence_id: 块的序号
- max_sequence_id: 总块数减1
- data: Base64编码的数据"""

code_json = '''# 读取测试文件并分块
with open(test_file, "rb") as f:
    file_bytes = f.read()

# 设置较小的块大小以便演示
batch_size = 100
batches = split_bytes_into_batches(file_bytes, batch_size)

# 显示第一个数据块的JSON格式
print("数据块的JSON格式示例：")
print(json.dumps(json.loads(batches[0].decode("utf-8")), indent=2, ensure_ascii=False))'''

# Selective QR code generation
markdown_selective = """## 2. 选择性QR码生成

系统支持选择性生成特定序号的QR码。这在需要重新生成丢失的数据块时特别有用。"""

code_selective = '''# 创建输出目录
output_dir = "qr_codes"
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# 首先生成前两个块的QR码
print("生成前两个数据块的QR码：")
convert_file_to_qr_codes(test_file, output_dir, batch_size, selected_indexes=[0, 1])

# 显示已生成的文件
print("\\n已生成的QR码文件：")
for file in sorted(os.listdir(output_dir)):
    print(file)'''

# Intermediate state
markdown_state = """## 3. 中间状态保存和恢复

系统能够保存扫描进度，并在需要时从中间状态恢复。这在处理大文件或需要分多次扫描时非常有用。"""

code_state = '''# 处理已生成的QR码
output_file = "reconstructed_file.txt"
intermediate_file = "scan_progress.json"

# 开始扫描并保存中间状态
missing_sequences = process_images_in_directory(output_dir, output_file, intermediate_file)

# 显示中间状态文件的内容
if os.path.exists(intermediate_file):
    with open(intermediate_file, "r") as f:
        state = json.load(f)
        print("\\n中间状态文件内容：")
        print(json.dumps({
            "max_sequence_id": state["max_sequence_id"],
            "scanned_chunks_count": len(state["scanned_chunks"])
        }, indent=2, ensure_ascii=False))'''

# Missing chunks detection
markdown_missing = """## 4. 缺失块检测和报告

系统会检查是否所有数据块都已收集，并报告任何缺失的块。"""

code_missing = '''if missing_sequences:
    print(f"\\n检测到缺失的数据块，序号为：{missing_sequences}")
    print("\\n现在生成缺失的数据块：")
    convert_file_to_qr_codes(test_file, output_dir, batch_size, selected_indexes=missing_sequences)
    
    print("\\n重新处理所有数据块：")
    missing_sequences = process_images_in_directory(output_dir, output_file, intermediate_file)
    
    if not missing_sequences:
        print("\\n文件重建成功！")
        with open(output_file, "r", encoding="utf-8") as f:
            print("\\n重建文件的内容：")
            print(f.read()[:100] + "...")'''

# Cleanup
markdown_cleanup = """## 清理临时文件

演示完成后，清理生成的临时文件。"""

code_cleanup = '''# 清理临时文件
import shutil

files_to_remove = [test_file, output_file, intermediate_file]
for file in files_to_remove:
    if os.path.exists(file):
        os.remove(file)
        print(f"已删除：{file}")

if os.path.exists(output_dir):
    shutil.rmtree(output_dir)
    print(f"已删除目录：{output_dir}")'''

# Add cells to notebook
cells = [
    nbf.v4.new_markdown_cell(markdown_intro),
    nbf.v4.new_code_cell(code_imports),
    nbf.v4.new_markdown_cell(markdown_missing_input),
    nbf.v4.new_code_cell(code_missing_input),
    nbf.v4.new_markdown_cell(markdown_path_handling),
    nbf.v4.new_code_cell(code_path_handling),
    nbf.v4.new_markdown_cell(markdown_test_prep),
    nbf.v4.new_code_cell(code_test_prep),
    nbf.v4.new_markdown_cell(markdown_json),
    nbf.v4.new_code_cell(code_json),
    nbf.v4.new_markdown_cell(markdown_selective),
    nbf.v4.new_code_cell(code_selective),
    nbf.v4.new_markdown_cell(markdown_state),
    nbf.v4.new_code_cell(code_state),
    nbf.v4.new_markdown_cell(markdown_missing),
    nbf.v4.new_code_cell(code_missing),
    nbf.v4.new_markdown_cell(markdown_cleanup),
    nbf.v4.new_code_cell(code_cleanup)
]

nb.cells = cells

# Write the notebook to a file
with open('qr_code_demo.ipynb', 'w', encoding='utf-8') as f:
    nbf.write(nb, f)
