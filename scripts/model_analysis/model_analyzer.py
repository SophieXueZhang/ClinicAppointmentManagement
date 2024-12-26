import os
import sys
from huggingface_hub import HfApi
from datetime import datetime, timedelta, timezone
import json
from tqdm import tqdm
import time
import pandas as pd
from openai import OpenAI
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

class ModelFetcher:
    def __init__(self):
        self.api = HfApi()
        self.output_dir = "downloaded_models"
        os.makedirs(self.output_dir, exist_ok=True)
        self.recent_days = 30  # Consider activity in last 30 days
        self.min_recent_downloads = 100  # Minimum downloads in recent period
        logging.info("ModelFetcher initialized")
        
    def is_gpu_processing_model(self, model):
        """Check if model is related to GPU data processing tasks"""
        relevant_tags = {
            'computer-vision', 'vision', 'image-classification',
            'object-detection', 'image-segmentation', 'image-to-image',
            'depth-estimation', 'video-classification', 'face-detection',
            'image-processing', 'visual', 'detection', 'classification',
            'feature-extraction', 'image-generation', 'pytorch', 'tensorflow',
            'deep-learning', 'neural-network', 'cuda', 'gpu'
        }
        
        # 获取模型标签和pipeline标签
        tags = set(model.tags if model.tags else [])
        pipeline_tag = model.pipeline_tag if model.pipeline_tag else ''
        
        # 检查模型名称和描述中的GPU相关关键词
        model_text = (model.modelId + ' ' + (model.card_data.get('description', '') if model.card_data else '')).lower()
        gpu_keywords = {'gpu', 'cuda', 'pytorch', 'tensorflow', 'deep learning', 'neural network'}
        has_gpu_keyword = any(keyword in model_text for keyword in gpu_keywords)
        
        # 返回True如果满足任一条件
        return bool(tags & relevant_tags) or pipeline_tag in relevant_tags or has_gpu_keyword
    
    def is_language_model(self, model):
        """Check if model is a language model"""
        llm_tags = {
            'text-generation', 'text2text-generation', 'conversational',
            'text-generation-inference', 'gpt', 'llm', 'chat'
        }
        
        if not model.tags:
            return False
            
        return bool(set(model.tags) & llm_tags)
    
    def has_ray_framework(self, model):
        """Check if model mentions Ray framework"""
        if not model.card_data:
            return False
            
        description = model.card_data.get('description', '').lower()
        return 'ray framework' in description or ' ray ' in description
    
    def calculate_popularity_score(self, model):
        """Calculate a popularity score based on recent activity"""
        if not model.lastModified:
            return 0
        
        days_since_update = (datetime.now(timezone.utc) - model.lastModified).days
        if days_since_update > self.recent_days:
            return 0
            
        # Calculate approximate recent downloads (assuming linear distribution)
        total_days = (datetime.now(timezone.utc) - model.created_at).days if model.created_at else 365
        daily_downloads = model.downloads / max(total_days, 1)
        recent_downloads = daily_downloads * min(self.recent_days, total_days)
        
        # Calculate score based on recent activity
        recency_factor = 1 - (days_since_update / self.recent_days)
        download_score = min(recent_downloads / self.min_recent_downloads, 1)
        like_score = model.likes / 100  # Normalize likes
        
        return (recency_factor * 0.4 + download_score * 0.4 + like_score * 0.2) * 100

    def fetch_models(self):
        """Fetch and filter models from HuggingFace"""
        print("正在从HuggingFace获取模型（目标：10个模型）...")
        try:
            # 获取更多模型以增加找到合适模型的概率
            models = list(self.api.list_models(
                sort="lastModified",
                direction=-1,
                full=True,
                limit=100  # 增加获取的模型数量
            ))
            
            print(f"获取到 {len(models)} 个原始模型")
            
            filtered_models = []
            for model in tqdm(models, desc="分析模型中"):
                try:
                    # 打印调试信息
                    print(f"\n检查模型: {model.modelId}")
                    print(f"标签: {model.tags}")
                    print(f"Pipeline标签: {model.pipeline_tag}")
                    
                    # 检查是否为GPU相关模型且不是语言模型
                    if not self.is_gpu_processing_model(model):
                        print("不是GPU处理相关模型，跳过")
                        continue
                        
                    if self.is_language_model(model):
                        print("是语言模型，跳过")
                        continue
                    
                    popularity_score = self.calculate_popularity_score(model)
                    print(f"流行度得分: {popularity_score}")
                    
                    if popularity_score <= 0:
                        print("流行度得分过低，跳过")
                        continue
                    
                    model_info = {
                        'name': model.modelId,
                        'downloads': model.downloads,
                        'likes': model.likes,
                        'tags': model.tags,
                        'last_modified': str(model.lastModified),
                        'uses_ray': self.has_ray_framework(model),
                        'description': model.card_data.get('description', '') if model.card_data else '',
                        'popularity_score': round(popularity_score, 2),
                        'created_at': str(model.created_at) if model.created_at else 'Unknown'
                    }
                    
                    print("找到符合条件的模型！")
                    filtered_models.append(model_info)
                    
                    if len(filtered_models) >= 10:
                        break
                        
                except Exception as e:
                    print(f"处理模型 {model.modelId} 时出错: {str(e)}")
                    continue
            
            print(f"\n成功获取 {len(filtered_models)} 个符合条件的模型")
            return filtered_models
            
        except Exception as e:
            print(f"获取模型时出错: {str(e)}")
            return []

class ModelAnalyzer(ModelFetcher):
    def __init__(self):
        super().__init__()
        self.client = OpenAI(
            api_key="sk-PXeE9gndo6ZxpNFr14IgfwkvcvO5E1Ha59CehfArVvsldbOw",
            base_url="https://api.moonshot.cn/v1"
        )
        
    def _get_default_analysis(self):
        """Return default analysis when LLM fails"""
        return {
            '输入数据': '无法获取',
            '输出数据': '无法获取',
            '数据处理目标': '无法获取',
            '数据处理方法': '无法获取',
            '可能应用场景': '无法获取'
        }
        
    def analyze_model_with_llm(self, model_info, max_retries=3):
        """Analyze model using Moonshot LLM API with retry mechanism"""
        prompt = f"""请分析这个深度学习模型的特征。模型信息如下：

模型名称: {model_info['name']}
描述: {model_info['description']}
标签: {', '.join(model_info['tags'])}

请严格按照以下格式提供分析结果（每个部分都必须包含）：

输入数据：[请具体描述该模型接收的输入数据类型，如图像、视频等]
输出数据：[请具体描述该模型产生的输出数据类型，如分类标签、边界框等]
数据处理目标：[请具体描述该模型的主要目标，如图像分类、目标检测等]
数据处理方法：[请详细描述该模型使用的具体技术和方法]
可能应用场景：[请列出该模型可能的实际应用场景]

请确保回答完整且格式统一。请用中文回答，每个部分都要填写具体内容，不要出现"无法解析"或空白内容。"""

        for attempt in range(max_retries):
            try:
                response = self.client.chat.completions.create(
                    model="moonshot-v1-8k",
                    messages=[{"role": "user", "content": prompt}],
                    temperature=0.7
                )
                return self._parse_llm_response(response.choices[0].message.content)
            except Exception as e:
                if attempt == max_retries - 1:
                    print(f"LLM analysis failed after {max_retries} attempts for model {model_info['name']}")
                    return self._get_default_analysis()
                time.sleep(2 ** attempt)  # Exponential backoff
                
    def _parse_llm_response(self, response):
        """Parse LLM response and extract required fields"""
        try:
            # Split response into sections
            sections = response.split('\n')
            result = {}
            
            # Initialize default values
            current_field = None
            current_content = []
            
            # Map of field indicators to keys
            field_map = {
                '输入数据：': '输入数据',
                '输出数据：': '输出数据',
                '数据处理目标：': '数据处理目标',
                '数据处理方法：': '数据处理方法',
                '可能应用场景：': '可能应用场景'
            }
            
            # Process each line
            for line in sections:
                line = line.strip()
                if not line:
                    continue
                    
                # Check if this line is a field indicator
                found_field = False
                for indicator, key in field_map.items():
                    if line.startswith(indicator):
                        if current_field and current_content:
                            result[current_field] = ' '.join(content for content in current_content if content and not any(content.startswith(ind) for ind in field_map.keys()))
                        current_field = key
                        content_start = line[len(indicator):].strip()
                        current_content = [content_start] if content_start else []
                        found_field = True
                        break
                
                if not found_field and current_field:
                    if not any(line.startswith(ind) for ind in field_map.keys()):
                        current_content.append(line)
            
            # Add the last field
            if current_field and current_content:
                result[current_field] = ' '.join(content for content in current_content if content and not any(content.startswith(ind) for ind in field_map.keys()))
            
            # Clean up and ensure all fields are present
            for key in field_map.values():
                if key not in result or not result[key]:
                    result[key] = '未提供具体信息'
                else:
                    # Remove any remaining field indicators and clean up the text
                    result[key] = result[key].strip()
                    if result[key].startswith('[') and result[key].endswith(']'):
                        result[key] = result[key][1:-1].strip()
                    
            return result
            
        except Exception as e:
            print(f"Error parsing LLM response: {str(e)}")
            return self._get_default_analysis()
            
    def fetch_models(self):
        """Fetch models and enhance with LLM analysis"""
        # Get models using parent class method
        models = super().fetch_models()
        enhanced_models = []
        
        print("\n开始进行模型分析...")
        for model in tqdm(models, desc="分析模型中"):
            analysis = self.analyze_model_with_llm(model)
            enhanced_model = {
                '模型名称': model['name'],
                '下载量': model['downloads'],
                '点赞数': model['likes'],
                '最后更新': model['last_modified'],
                '输入数据': analysis['输入数据'],
                '输出数据': analysis['输出数据'],
                '数据处理目标': analysis['数据处理目标'],
                '数据处理方法': analysis['数据处理方法'],
                '可能应用场景': analysis['可能应用场景'],
                '使用Ray框架': model['uses_ray'],
                'Ray框架说明': '使用Ray框架进行分布式计算' if model['uses_ray'] else ''
            }
            enhanced_models.append(enhanced_model)
            
        return enhanced_models
        
    def save_models_csv(self, models):
        """Save models data to CSV file with proper encoding for Chinese characters"""
        df = pd.DataFrame(models)
        output_file = os.path.join(self.output_dir, 'filtered_models.csv')
        df.to_csv(output_file, index=False, encoding='utf-8-sig')
        print(f"\n模型数据已保存至: {output_file}")
        return output_file

def main():
    """Main execution function"""
    try:
        print("初始化模型分析器...")
        analyzer = ModelAnalyzer()
        
        print("获取并分析模型中...")
        models = analyzer.fetch_models()
        
        print(f"\n成功分析 {len(models)} 个模型")
        output_file = analyzer.save_models_csv(models)
        print(f"分析结果已保存至: {output_file}")
        
    except Exception as e:
        print(f"执行过程中发生错误: {str(e)}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
