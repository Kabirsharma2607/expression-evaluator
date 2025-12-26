'use client';

import React, { useState, useCallback, useEffect } from 'react';
import Editor, { DiffEditor } from '@monaco-editor/react';
import { Save, Upload, RefreshCw, GitBranch, Eye, Code, FilePlus } from 'lucide-react';
import * as yaml from 'js-yaml';

interface PolicyEditorProps {
  policyName: string;
  initialContent: string;
  onSave: (content: string) => Promise<void>;
  onRefresh: () => Promise<void>;
}

export default function PolicyEditor({ policyName, initialContent, onSave, onRefresh }: PolicyEditorProps) {
  const [content, setContent] = useState(initialContent);
  const [originalContent, setOriginalContent] = useState(initialContent);
  const [isDirty, setIsDirty] = useState(false);
  const [viewMode, setViewMode] = useState<'edit' | 'diff' | 'preview'>('edit');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setContent(initialContent);
    setOriginalContent(initialContent);
    setIsDirty(false);
  }, [initialContent]);

  const handleEditorChange = useCallback((value: string | undefined) => {
    if (value !== undefined) {
      setContent(value);
      setIsDirty(value !== originalContent);
      setError(null);

      // Validate YAML syntax
      try {
        yaml.load(value);
      } catch (e) {
        setError(`YAML Syntax Error: ${(e as Error).message}`);
      }
    }
  }, [originalContent]);

  const handleSave = async () => {
    if (error) {
      alert('Cannot save: YAML syntax error');
      return;
    }

    setSaving(true);
    try {
      await onSave(content);
      setOriginalContent(content);
      setIsDirty(false);
    } catch (error) {
      console.error('Save failed:', error);
      alert('Failed to save policy');
    } finally {
      setSaving(false);
    }
  };

  const handleRefresh = async () => {
    if (isDirty) {
      const confirm = window.confirm('You have unsaved changes. Are you sure you want to refresh?');
      if (!confirm) return;
    }
    await onRefresh();
  };

  const renderPreview = () => {
    try {
      const parsed = yaml.load(content) as any;
      return (
        <div className="p-4 bg-gray-50 rounded-lg overflow-auto h-full">
          <h3 className="text-lg font-semibold mb-2">{parsed.policyName || 'Unnamed Policy'}</h3>
          <p className="text-gray-600 mb-4">{parsed.description || 'No description'}</p>
          <p className="text-sm text-gray-500 mb-4">Version: {parsed.version || 'N/A'}</p>

          <div className="space-y-4">
            <h4 className="font-medium">Rules:</h4>
            {parsed.rules && Object.entries(parsed.rules).map(([name, rule]: [string, any]) => (
              <div key={name} className="border rounded p-3 bg-white">
                <h5 className="font-medium text-blue-600">{name}</h5>
                <p className="text-sm text-gray-600 mt-1">{rule.description}</p>
                <code className="block mt-2 p-2 bg-gray-100 rounded text-xs">
                  {rule.expression}
                </code>
                {rule.dependencies && rule.dependencies.length > 0 && (
                  <p className="text-xs text-gray-500 mt-2">
                    Dependencies: {rule.dependencies.join(', ')}
                  </p>
                )}
              </div>
            ))}
          </div>
        </div>
      );
    } catch (e) {
      return (
        <div className="p-4 text-red-600">
          Error parsing YAML: {(e as Error).message}
        </div>
      );
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b bg-white">
        <div className="flex items-center space-x-4">
          <h2 className="text-xl font-semibold">{policyName}.yml</h2>
          {isDirty && <span className="text-sm text-orange-500">● Modified</span>}
        </div>

        <div className="flex items-center space-x-2">
          {/* View Mode Toggles */}
          <div className="flex items-center bg-gray-100 rounded-lg p-1">
            <button
              onClick={() => setViewMode('edit')}
              className={`px-3 py-1 rounded flex items-center space-x-1 ${
                viewMode === 'edit' ? 'bg-white shadow' : ''
              }`}
            >
              <Code size={16} />
              <span>Edit</span>
            </button>
            <button
              onClick={() => setViewMode('diff')}
              className={`px-3 py-1 rounded flex items-center space-x-1 ${
                viewMode === 'diff' ? 'bg-white shadow' : ''
              }`}
            >
              <GitBranch size={16} />
              <span>Diff</span>
            </button>
            <button
              onClick={() => setViewMode('preview')}
              className={`px-3 py-1 rounded flex items-center space-x-1 ${
                viewMode === 'preview' ? 'bg-white shadow' : ''
              }`}
            >
              <Eye size={16} />
              <span>Preview</span>
            </button>
          </div>

          {/* Action Buttons */}
          <button
            onClick={handleRefresh}
            className="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg flex items-center space-x-2"
          >
            <RefreshCw size={16} />
            <span>Refresh</span>
          </button>

          <button
            onClick={handleSave}
            disabled={!isDirty || saving || !!error}
            className={`px-4 py-2 rounded-lg flex items-center space-x-2 ${
              isDirty && !error
                ? 'bg-blue-500 hover:bg-blue-600 text-white'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            {saving ? <RefreshCw size={16} className="animate-spin" /> : <Save size={16} />}
            <span>{saving ? 'Saving...' : 'Save'}</span>
          </button>
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-3 text-red-700 text-sm">
          {error}
        </div>
      )}

      {/* Editor Content */}
      <div className="flex-1 overflow-hidden">
        {viewMode === 'edit' && (
          <Editor
            height="100%"
            defaultLanguage="yaml"
            value={content}
            onChange={handleEditorChange}
            theme="vs-light"
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              lineNumbers: 'on',
              roundedSelection: false,
              scrollBeyondLastLine: false,
              wordWrap: 'on',
              automaticLayout: true,
            }}
          />
        )}

        {viewMode === 'diff' && (
          <DiffEditor
            height="100%"
            language="yaml"
            original={originalContent}
            modified={content}
            theme="vs-light"
            onMount={(editor) => {
              // Get the modified editor (right side)
              const modifiedEditor = editor.getModifiedEditor();

              // Listen for changes in the modified editor
              modifiedEditor.onDidChangeModelContent(() => {
                const newContent = modifiedEditor.getValue();
                setContent(newContent);
                setIsDirty(newContent !== originalContent);
                setError(null);

                // Validate YAML syntax
                try {
                  yaml.load(newContent);
                } catch (e) {
                  setError(`YAML Syntax Error: ${(e as Error).message}`);
                }
              });
            }}
            options={{
              renderSideBySide: true,
              minimap: { enabled: false },
              fontSize: 14,
              readOnly: false,
              originalEditable: false,  // Make only the right side editable
            }}
          />
        )}

        {viewMode === 'preview' && renderPreview()}
      </div>
    </div>
  );
}