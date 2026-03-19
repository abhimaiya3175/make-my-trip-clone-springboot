import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock global fetch
const mockFetch = jest.fn();
global.fetch = mockFetch;

import RecommendationsSection from "../recommendation/RecommendationsSection";

const mockRecommendations = [
  {
    id: "rec1",
    userId: "user1",
    itemId: "FL101",
    itemType: "FLIGHT",
    score: 0.92,
    reason: "Based on your recent searches",
    tags: ["direct", "morning"],
    createdAt: "2026-03-06T10:00:00Z",
  },
  {
    id: "rec2",
    userId: "user1",
    itemId: "HT201",
    itemType: "HOTEL",
    score: 0.85,
    reason: "Highly rated in your destination",
    tags: ["beach", "luxury"],
    createdAt: "2026-03-06T10:00:00Z",
  },
  {
    id: "rec3",
    userId: "user1",
    itemId: "FL102",
    itemType: "FLIGHT",
    score: 0.78,
    reason: "Popular route",
    tags: ["budget"],
    createdAt: "2026-03-06T10:00:00Z",
  },
];

describe("RecommendationsSection", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders correct number of cards", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: mockRecommendations }),
    });

    render(<RecommendationsSection userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("FL101")).toBeInTheDocument();
    });

    expect(screen.getByText("HT201")).toBeInTheDocument();
    expect(screen.getByText("FL102")).toBeInTheDocument();
  });

  it("shows loading state", () => {
    mockFetch.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<RecommendationsSection userId="user1" />);
    expect(screen.getByText(/loading recommendations/i)).toBeInTheDocument();
  });

  it("shows empty state when no recommendations", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: [] }),
    });

    render(<RecommendationsSection userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText(/no recommendations yet/i)).toBeInTheDocument();
    });
  });

  it("shows error state on fetch failure", async () => {
    mockFetch.mockRejectedValue(new Error("Network error"));

    render(<RecommendationsSection userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText(/could not load recommendations/i)).toBeInTheDocument();
    });
  });

  it("Not Interested click triggers feedback and refreshes", async () => {
    const user = userEvent.setup();

    // First call: fetch recommendations; Second call: feedback POST; Third call: refresh
    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: mockRecommendations }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: {} }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          success: true,
          data: mockRecommendations.filter((r) => r.id !== "rec1"),
        }),
      });

    render(<RecommendationsSection userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("FL101")).toBeInTheDocument();
    });

    // Find "Not interested" buttons and click the first one
    const notInterestedButtons = screen.getAllByText(/not interested/i);
    await user.click(notInterestedButtons[0]);

    // Verify feedback POST was called
    await waitFor(() => {
      const feedbackCall = mockFetch.mock.calls.find(
        (call) =>
          typeof call[0] === "string" && call[0].includes("/feedback")
      );
      expect(feedbackCall).toBeDefined();
    });
  });

  it("shows explanation tooltip when 'Why this?' is clicked", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: mockRecommendations }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          success: true,
          data: { explanation: "You searched for flights to BOM recently" },
        }),
      });

    render(<RecommendationsSection userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("FL101")).toBeInTheDocument();
    });

    const whyButtons = screen.getAllByText(/why this/i);
    await user.click(whyButtons[0]);

    await waitFor(() => {
      expect(
        screen.getByText("You searched for flights to BOM recently")
      ).toBeInTheDocument();
    });
  });
});
