import React, { useState } from 'react';
import { ThumbsUp } from 'lucide-react';

interface HelpfulButtonProps {
  reviewId: string;
  initialCount: number;
  initialVoted: boolean;
  userId?: string;
  onVote: (reviewId: string) => Promise<void>;
}

export const HelpfulButton: React.FC<HelpfulButtonProps> = ({
  reviewId,
  initialCount,
  initialVoted,
  userId,
  onVote
}) => {
  const [count, setCount] = useState(initialCount);
  const [voted, setVoted] = useState(initialVoted);
  const [loading, setLoading] = useState(false);

  const handleVote = async () => {
    if (!userId) {
      alert('Please login to vote');
      return;
    }

    setLoading(true);
    try {
      await onVote(reviewId);
      setVoted(!voted);
      setCount(voted ? count - 1 : count + 1);
    } catch (error) {
      console.error('Vote failed:', error);
      alert('Failed to vote. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <button
      onClick={handleVote}
      disabled={loading}
      className={`
        flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium
        transition-all duration-200 border
        ${voted 
          ? 'bg-blue-50 text-blue-700 border-blue-300 hover:bg-blue-100' 
          : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
        }
        ${loading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
      `}
    >
      <ThumbsUp 
        className={`w-4 h-4 ${voted ? 'fill-blue-700' : ''}`} 
      />
      <span>Helpful</span>
      {count > 0 && (
        <span className="font-semibold">({count})</span>
      )}
    </button>
  );
};
