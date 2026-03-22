import React, { useState } from "react";
import { X, Image } from "lucide-react";
import StarRating from "./StarRating";
import { REQUEST_STATE, getErrorMessage } from "@/utils/requestState";

interface Review {
  id: string;
  rating: number;
  text: string;
  photos?: string[];
}

interface EditReviewModalProps {
  review: Review;
  onClose: () => void;
  onSave: (data: { rating: number; text: string; photos?: string[] }) => Promise<void>;
}

const EditReviewModal: React.FC<EditReviewModalProps> = ({ review, onClose, onSave }) => {
  const [rating, setRating] = useState(review.rating);
  const [text, setText] = useState(review.text);
  const [photos, setPhotos] = useState<File[]>([]);
  const [photoPreviews, setPhotoPreviews] = useState<string[]>([]);
  const [existingPhotos, setExistingPhotos] = useState<string[]>(review.photos || []);
  const [requestState, setRequestState] = useState(REQUEST_STATE.IDLE);
  const [error, setError] = useState<string | null>(null);
  const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files) return;
    const selectedFiles = Array.from(e.target.files);
    
    const totalPhotos = existingPhotos.length + photos.length + selectedFiles.length;
    if (totalPhotos > 5) {
      setError(`Maximum 5 photos allowed (currently ${existingPhotos.length + photos.length})`);
      return;
    }

    for (const file of selectedFiles) {
      if (file.size > 5 * 1024 * 1024) {
        setError(`File ${file.name} exceeds 5MB limit`);
        return;
      }
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
        setError(`File ${file.name} is not a supported format (JPEG, PNG, WEBP)`);
        return;
      }
    }

    const newPhotos = [...photos, ...selectedFiles];
    setPhotos(newPhotos);
    
    const newPreviews = selectedFiles.map(f => URL.createObjectURL(f));
    setPhotoPreviews([...photoPreviews, ...newPreviews]);
    setError(null);
    
    e.target.value = '';
  };

  const handleRemoveNewPhoto = (index: number) => {
    URL.revokeObjectURL(photoPreviews[index]);
    setPhotos(photos.filter((_, i) => i !== index));
    setPhotoPreviews(photoPreviews.filter((_, i) => i !== index));
  };

  const handleRemoveExistingPhoto = (index: number) => {
    setExistingPhotos(existingPhotos.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (rating === 0) {
      setError('Please select a rating');
      return;
    }

    if (!text.trim()) {
      setError('Please write a review');
      return;
    }

    if (text.length > 2000) {
      setError('Review must be less than 2000 characters');
      return;
    }

    setRequestState(REQUEST_STATE.LOADING);

    try {
      const token =
        typeof window !== "undefined" && localStorage
          ? localStorage.getItem("authToken")
          : null;

      if (!token) {
        throw new Error("Authentication required. Please login again.");
      }

      const uploadedUrls: string[] = [...existingPhotos];
      for (const file of photos) {
        const formData = new FormData();
        formData.append('photo', file);

        const res = await fetch(`${API_BASE}/api/reviews/upload-photo`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        });
        
        const json = await res.json();
        if (!res.ok) {
          throw new Error(json.error?.message || 'Failed to upload photo');
        }
        uploadedUrls.push(json.data);
      }

      await onSave({
        rating,
        text: text.trim(),
        photos: uploadedUrls.length > 0 ? uploadedUrls : undefined
      });

      setRequestState(REQUEST_STATE.SUCCESS);
    } catch (err) {
      setRequestState(REQUEST_STATE.ERROR);
      setError(getErrorMessage(err));
    }
  };

  const resolvePhotoUrl = (photoUrl: string) => {
    if (!photoUrl) return "";
    if (/^https?:\/\//i.test(photoUrl) || /^data:/i.test(photoUrl)) {
      return photoUrl;
    }

    const normalizedPath = photoUrl.startsWith("/") ? photoUrl : `/${photoUrl}`;
    const explicitBackend =
      process.env.NEXT_PUBLIC_BACKEND_URL || process.env.NEXT_PUBLIC_API_URL;

    if (typeof window !== "undefined" && !explicitBackend) {
      return `/backend-api${normalizedPath}`;
    }

    const backendBase = (explicitBackend || "http://localhost:8080").replace(/\/$/, "");
    return `${backendBase}${normalizedPath}`;
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b flex items-center justify-between p-6">
          <h2 className="text-xl font-bold">Edit Review</h2>
          <button
            onClick={onClose}
            className="p-1 hover:bg-gray-100 rounded"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Rating */}
          <div>
            <label className="block text-sm font-medium mb-2">Rating</label>
            <div className="flex gap-2">
              {[1, 2, 3, 4, 5].map(star => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setRating(star)}
                  className="focus:outline-none"
                >
                  <svg
                    className={`w-8 h-8 ${
                      star <= rating
                        ? 'text-yellow-400 fill-current'
                        : 'text-gray-300'
                    }`}
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                </button>
              ))}
            </div>
          </div>

          {/* Text */}
          <div>
            <label className="block text-sm font-medium mb-2">
              Review Text ({text.length}/2000)
            </label>
            <textarea
              value={text}
              onChange={(e) => setText(e.target.value.substring(0, 2000))}
              className="w-full border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              rows={5}
              placeholder="Share your experience..."
            />
          </div>

          {/* Existing Photos */}
          {existingPhotos.length > 0 && (
            <div>
              <label className="block text-sm font-medium mb-2">Current Photos</label>
              <div className="flex gap-2 flex-wrap">
                {existingPhotos.map((photo, index) => (
                  <div key={index} className="relative">
                    <img
                      src={resolvePhotoUrl(photo)}
                      alt={`Review photo ${index + 1}`}
                      className="w-24 h-24 object-cover rounded-md border border-gray-200"
                    />
                    <button
                      type="button"
                      onClick={() => handleRemoveExistingPhoto(index)}
                      className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* New Photos */}
          {photoPreviews.length > 0 && (
            <div>
              <label className="block text-sm font-medium mb-2">New Photos</label>
              <div className="flex gap-2 flex-wrap">
                {photoPreviews.map((preview, index) => (
                  <div key={index} className="relative">
                    <img
                      src={preview}
                      alt={`New photo ${index + 1}`}
                      className="w-24 h-24 object-cover rounded-md border border-gray-200"
                    />
                    <button
                      type="button"
                      onClick={() => handleRemoveNewPhoto(index)}
                      className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Photo Upload */}
          <div>
            <label className="block text-sm font-medium mb-2">
              Add Photos ({existingPhotos.length + photoPreviews.length}/5)
            </label>
            <label className="flex items-center justify-center gap-2 border-2 border-dashed border-gray-300 rounded-lg p-4 cursor-pointer hover:border-blue-500 hover:bg-blue-50 transition">
              <Image className="w-5 h-5" />
              <span className="text-sm text-gray-600">Click to add photos</span>
              <input
                type="file"
                multiple
                accept="image/jpeg,image/png,image/webp"
                onChange={handleFileSelect}
                className="hidden"
              />
            </label>
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Buttons */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button
              type="button"
              onClick={onClose}
              disabled={requestState === REQUEST_STATE.LOADING}
              className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={requestState === REQUEST_STATE.LOADING}
              className="px-4 py-2 bg-blue-600 text-white hover:bg-blue-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
            >
              {requestState === REQUEST_STATE.LOADING ? (
                <>
                  <span className="animate-spin">⏳</span>
                  Saving...
                </>
              ) : (
                'Save Changes'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditReviewModal;
