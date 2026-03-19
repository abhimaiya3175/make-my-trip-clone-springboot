import React, { useState } from 'react';
import { MessageCircle, Send } from 'lucide-react';

interface Reply {
  userId: string;
  userName: string;
  text: string;
  isOwner: boolean;
  timestamp: string;
}

interface ReplySectionProps {
  reviewId: string;
  replies: Reply[];
  userId?: string;
  userName?: string;
  isOwner?: boolean;
  onReply: (reviewId: string, text: string, userName: string, isOwner: boolean) => Promise<void>;
}

export const ReplySection: React.FC<ReplySectionProps> = ({
  reviewId,
  replies,
  userId,
  userName,
  isOwner = false,
  onReply
}) => {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyText, setReplyText] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!userId) {
      alert('Please login to reply');
      return;
    }

    if (!replyText.trim()) {
      alert('Reply cannot be empty');
      return;
    }

    setLoading(true);
    try {
      await onReply(reviewId, replyText.trim(), userName || 'Anonymous', isOwner);
      setReplyText('');
      setShowReplyForm(false);
    } catch (error) {
      console.error('Reply failed:', error);
      alert('Failed to post reply. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mt-4 border-t pt-4">
      {/* Existing Replies */}
      {replies.length > 0 && (
        <div className="space-y-3 mb-4">
          {replies.map((reply, index) => (
            <div 
              key={index} 
              className={`
                pl-4 border-l-2 
                ${reply.isOwner ? 'border-blue-500 bg-blue-50' : 'border-gray-300 bg-gray-50'}
                p-3 rounded-r
              `}
            >
              <div className="flex items-center gap-2 mb-1">
                <span className="font-semibold text-sm">{reply.userName}</span>
                {reply.isOwner && (
                  <span className="text-xs bg-blue-600 text-white px-2 py-0.5 rounded">
                    Owner
                  </span>
                )}
                <span className="text-xs text-gray-500">
                  {new Date(reply.timestamp).toLocaleDateString()}
                </span>
              </div>
              <p className="text-sm text-gray-700">{reply.text}</p>
            </div>
          ))}
        </div>
      )}

      {/* Reply Button / Form */}
      {!showReplyForm ? (
        <button
          onClick={() => setShowReplyForm(true)}
          className="flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700 font-medium"
        >
          <MessageCircle className="w-4 h-4" />
          Reply
        </button>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-2">
          <textarea
            value={replyText}
            onChange={(e) => setReplyText(e.target.value)}
            placeholder={isOwner ? "Reply as owner..." : "Write your reply..."}
            className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            rows={3}
          />
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => {
                setShowReplyForm(false);
                setReplyText('');
              }}
              className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 text-sm"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !replyText.trim()}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 text-sm"
            >
              <Send className="w-4 h-4" />
              {loading ? 'Posting...' : 'Post Reply'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
