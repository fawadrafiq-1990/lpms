import { useState } from 'react';
import { Bot, Send } from 'lucide-react';

export default function AIAssistantPage() {
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState<Array<{role: string; content: string}>>([]);

  const handleSend = () => {
    if (!message.trim()) return;
    setMessages([...messages, { role: 'user', content: message }]);
    setMessage('');
    // AI response would come from backend
    setTimeout(() => {
      setMessages(prev => [...prev, { role: 'assistant', content: 'AI response will appear here. Connect to the AI backend for actual responses.' }]);
    }, 1000);
  };

  return (
    <div className="space-y-6 animate-in">
      <div className="page-header">
        <div><h1 className="page-title">AI Legal Assistant</h1><p className="page-subtitle">Ask questions about cases, documents, and legal research</p></div>
      </div>
      <div className="card" style={{height: 'calc(100vh - 250px)', minHeight: '500px'}}>
        <div className="flex flex-col h-full">
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {messages.length === 0 && (
              <div className="text-center py-12">
                <Bot className="w-16 h-16 text-primary-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-neutral-700 mb-2">Legal AI Assistant</h3>
                <p className="text-neutral-500 max-w-md mx-auto">Ask me anything about your cases, documents, legal research, or get help with case analysis.</p>
              </div>
            )}
            {messages.map((msg, i) => (
              <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-lg px-4 py-3 rounded-lg ${msg.role === 'user' ? 'bg-primary-600 text-white' : 'bg-neutral-100'}`}>
                  {msg.content}
                </div>
              </div>
            ))}
          </div>
          <div className="p-4 border-t">
            <div className="flex gap-2">
              <input type="text" value={message} onChange={e => setMessage(e.target.value)} onKeyPress={e => e.key === 'Enter' && handleSend()} placeholder="Ask the AI assistant..." className="input flex-1" />
              <button onClick={handleSend} className="btn-primary"><Send className="w-4 h-4" /></button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
