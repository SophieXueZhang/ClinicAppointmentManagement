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
