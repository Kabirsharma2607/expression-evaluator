'use client';

import { useState, useEffect } from 'react';
import PolicyEditor from '@/components/PolicyEditor';
import {
  FileText,
  Plus,
  RefreshCw,
  Trash2,
  Database,
  Clock,
  ChevronRight,
  Search,
  Settings
} from 'lucide-react';
import axios from 'axios';

interface Policy {
  name: string;
  lastModified?: Date;
}

export default function Dashboard() {
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [selectedPolicy, setSelectedPolicy] = useState<string | null>(null);
  const [policyContent, setPolicyContent] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [showNewPolicyDialog, setShowNewPolicyDialog] = useState(false);
  const [newPolicyName, setNewPolicyName] = useState('');
  const [cacheStatus, setCacheStatus] = useState<'idle' | 'evicting' | 'refreshing'>('idle');

  useEffect(() => {
    loadPolicies();
  }, []);

  const loadPolicies = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/policies');
      setPolicies(response.data.policies.map((name: string) => ({ name })));
    } catch (error) {
      console.error('Failed to load policies:', error);
      alert('Failed to load policies');
    } finally {
      setLoading(false);
    }
  };

  const loadPolicy = async (policyName: string) => {
    setLoading(true);
    try {
      const response = await axios.get(`/api/policies/${policyName}`);
      setPolicyContent(response.data.content);
      setSelectedPolicy(policyName);
    } catch (error) {
      console.error('Failed to load policy:', error);
      alert('Failed to load policy');
    } finally {
      setLoading(false);
    }
  };

  const savePolicy = async (content: string) => {
    if (!selectedPolicy) return;

    try {
      await axios.put(`/api/policies/${selectedPolicy}`, { content });

      // Try to refresh cache, but don't fail if it doesn't work
      try {
        await refreshCache();
      } catch (cacheError) {
        console.warn('Cache refresh failed, but policy was saved:', cacheError);
      }

      alert('Policy saved successfully!');
    } catch (error) {
      console.error('Failed to save policy:', error);
      throw error;
    }
  };

  const createNewPolicy = async () => {
    if (!newPolicyName) {
      alert('Please enter a policy name');
      return;
    }

    try {
      await axios.post('/api/policies', { policyName: newPolicyName });
      await loadPolicies();
      setShowNewPolicyDialog(false);
      setNewPolicyName('');
      await loadPolicy(newPolicyName);
    } catch (error) {
      console.error('Failed to create policy:', error);
      alert('Failed to create policy');
    }
  };

  const evictCache = async () => {
    setCacheStatus('evicting');
    try {
      await axios.post('/api/cache', { action: 'evict' });
      alert('Cache evicted successfully');
    } catch (error) {
      console.error('Failed to evict cache:', error);
      alert('Failed to evict cache');
    } finally {
      setCacheStatus('idle');
    }
  };

  const refreshCache = async () => {
    setCacheStatus('refreshing');
    try {
      await axios.post('/api/cache', { action: 'refresh' });
      alert('Cache refreshed successfully');
    } catch (error) {
      console.error('Failed to refresh cache:', error);
      alert('Failed to refresh cache');
    } finally {
      setCacheStatus('idle');
    }
  };

  const refreshCurrentPolicy = async () => {
    if (selectedPolicy) {
      await loadPolicy(selectedPolicy);
    }
  };

  const filteredPolicies = policies.filter(p =>
    p.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <div className="w-80 bg-white border-r border-gray-200 flex flex-col">
        {/* Sidebar Header */}
        <div className="p-4 border-b border-gray-200">
          <h1 className="text-xl font-bold text-gray-800">Policy Editor</h1>
          <p className="text-sm text-gray-500 mt-1">Manage Expression Evaluator Policies</p>
        </div>

        {/* Search Bar */}
        <div className="p-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
            <input
              type="text"
              placeholder="Search policies..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* Policy List */}
        <div className="flex-1 overflow-y-auto px-4">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-gray-600">Policies</h3>
            <button
              onClick={() => setShowNewPolicyDialog(true)}
              className="text-blue-500 hover:text-blue-600"
            >
              <Plus size={18} />
            </button>
          </div>

          {loading && !selectedPolicy ? (
            <div className="flex items-center justify-center py-8">
              <RefreshCw className="animate-spin" size={20} />
            </div>
          ) : (
            <div className="space-y-1">
              {filteredPolicies.map((policy) => (
                <button
                  key={policy.name}
                  onClick={() => loadPolicy(policy.name)}
                  className={`w-full text-left px-3 py-2 rounded-lg flex items-center justify-between group hover:bg-gray-100 ${
                    selectedPolicy === policy.name ? 'bg-blue-50 text-blue-600' : ''
                  }`}
                >
                  <div className="flex items-center space-x-2">
                    <FileText size={16} />
                    <span className="text-sm">{policy.name}</span>
                  </div>
                  <ChevronRight size={14} className="opacity-0 group-hover:opacity-100" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Cache Controls */}
        <div className="p-4 border-t border-gray-200">
          <h3 className="text-sm font-semibold text-gray-600 mb-3">Cache Management</h3>
          <div className="space-y-2">
            <button
              onClick={evictCache}
              disabled={cacheStatus !== 'idle'}
              className="w-full px-3 py-2 bg-orange-100 hover:bg-orange-200 text-orange-700 rounded-lg flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {cacheStatus === 'evicting' ? (
                <RefreshCw className="animate-spin" size={16} />
              ) : (
                <Trash2 size={16} />
              )}
              <span>Evict Cache</span>
            </button>
            <button
              onClick={refreshCache}
              disabled={cacheStatus !== 'idle'}
              className="w-full px-3 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-lg flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {cacheStatus === 'refreshing' ? (
                <RefreshCw className="animate-spin" size={16} />
              ) : (
                <Database size={16} />
              )}
              <span>Refresh Cache</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {selectedPolicy ? (
          <PolicyEditor
            policyName={selectedPolicy}
            initialContent={policyContent}
            onSave={savePolicy}
            onRefresh={refreshCurrentPolicy}
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <FileText size={48} className="text-gray-300 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-gray-600">No Policy Selected</h2>
              <p className="text-gray-500 mt-2">Select a policy from the sidebar to edit</p>
            </div>
          </div>
        )}
      </div>

      {/* New Policy Dialog */}
      {showNewPolicyDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96">
            <h2 className="text-xl font-semibold mb-4">Create New Policy</h2>
            <input
              type="text"
              placeholder="Policy name (without .yml)"
              value={newPolicyName}
              onChange={(e) => setNewPolicyName(e.target.value)}
              className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 mb-4"
              autoFocus
            />
            <div className="flex justify-end space-x-2">
              <button
                onClick={() => {
                  setShowNewPolicyDialog(false);
                  setNewPolicyName('');
                }}
                className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={createNewPolicy}
                className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg"
              >
                Create
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}