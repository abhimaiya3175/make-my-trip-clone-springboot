import { useEffect, useState, useCallback, useRef } from "react";

interface UseSSEOptions {
  url: string;
  onMessage?: (data: any) => void;
  onError?: (error: Event) => void;
  autoReconnect?: boolean;
  reconnectDelay?: number;
}

interface UseSSEReturn {
  data: any;
  connected: boolean;
  error: boolean;
  close: () => void;
}

export function useSSE({
  url,
  onMessage,
  onError,
  autoReconnect = true,
  reconnectDelay = 3000,
}: UseSSEOptions): UseSSEReturn {
  const [data, setData] = useState<any>(null);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const close = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    setConnected(false);
  }, []);

  const connect = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      setConnected(true);
      setError(false);
    };

    es.onmessage = (event) => {
      try {
        const parsed = JSON.parse(event.data);
        setData(parsed);
        onMessage?.(parsed);
      } catch {
        setData(event.data);
        onMessage?.(event.data);
      }
    };

    es.onerror = (event) => {
      setError(true);
      setConnected(false);
      onError?.(event);
      es.close();

      if (autoReconnect) {
        reconnectTimeoutRef.current = setTimeout(() => {
          connect();
        }, reconnectDelay);
      }
    };
  }, [url, onMessage, onError, autoReconnect, reconnectDelay]);

  useEffect(() => {
    if (url) {
      connect();
    }
    return () => {
      close();
    };
  }, [url, connect, close]);

  return { data, connected, error, close };
}

export default useSSE;
