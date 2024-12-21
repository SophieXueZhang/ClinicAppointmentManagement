'use client';

import React from 'react';
import dynamic from 'next/dynamic';
import 'react-quill/dist/quill.snow.css';
import { useTheme } from 'next-themes';

const QuillNoSSR = dynamic(() => import('react-quill'), {
  ssr: false,
  loading: () => <p>加载编辑器中...</p>,
});

interface QuillEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

const modules = {
  toolbar: [
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
    [{ 'indent': '-1'}, { 'indent': '+1' }],
    [{ 'direction': 'rtl' }],
    [{ 'color': [] }, { 'background': [] }],
    ['link', 'image', 'video'],
    ['clean']
  ],
};

const formats = [
  'header',
  'bold', 'italic', 'underline', 'strike',
  'list', 'bullet', 'indent',
  'link', 'image', 'video',
  'direction',
  'color', 'background'
];

export function QuillEditor({ value, onChange, placeholder = '请输入内容...' }: QuillEditorProps) {
  const { theme } = useTheme();

  return (
    <div className={`${theme === 'dark' ? 'quill-dark' : 'quill-light'}`}>
      <style dangerouslySetInnerHTML={{ __html: `
        .quill-dark .ql-toolbar {
          background-color: #1a1a1a !important;
          border-color: #2d2d2d !important;
        }
        .quill-dark .ql-container {
          background-color: #1a1a1a !important;
          border-color: #2d2d2d !important;
          color: #ffffff !important;
        }
        .quill-dark .ql-editor {
          color: #ffffff !important;
        }
        .quill-dark .ql-stroke {
          stroke: #ffffff !important;
        }
        .quill-dark .ql-fill {
          fill: #ffffff !important;
        }
        .quill-dark .ql-picker {
          color: #ffffff !important;
        }
        .quill-dark .ql-picker-options {
          background-color: #1a1a1a !important;
          border-color: #2d2d2d !important;
        }
      ` }} />
      <QuillNoSSR
        value={value}
        onChange={onChange}
        modules={modules}
        formats={formats}
        placeholder={placeholder}
        theme="snow"
        bounds=".quill-editor"
      />
    </div>
  );
}
