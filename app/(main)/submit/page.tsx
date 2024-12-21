'use client';

import { useState } from 'react';
import { QuillEditor } from '@/components/quill-editor';

export default function SubmitPage() {
  const [content, setContent] = useState('');
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log({ title, category, content });
  };

  return (
    <div className="container mx-auto py-8">
      <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
        <h1 className="text-2xl font-bold mb-6">创建投稿</h1>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="title" className="block text-sm font-medium mb-2">标题</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="请输入标题"
              className="w-full p-2 border rounded"
            />
          </div>
          
          <div>
            <label htmlFor="category" className="block text-sm font-medium mb-2">分类</label>
            <select
              id="category"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full p-2 border rounded"
            >
              <option value="">选择分类</option>
              <option value="technology">技术</option>
              <option value="design">设计</option>
              <option value="resources">资源</option>
              <option value="other">其他</option>
            </select>
          </div>

          <div>
            <label htmlFor="content" className="block text-sm font-medium mb-2">内容</label>
            <div className="min-h-[400px]">
              <QuillEditor
                value={content}
                onChange={setContent}
                placeholder="请输入内容..."
              />
            </div>
          </div>

          <button 
            type="submit" 
            className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
          >
            提交
          </button>
        </form>
      </div>
    </div>
  );
}
