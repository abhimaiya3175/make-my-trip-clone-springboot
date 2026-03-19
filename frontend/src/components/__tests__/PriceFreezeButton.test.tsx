import React from "react";
import { render, screen, waitFor, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock global fetch
const mockFetch = jest.fn();
global.fetch = mockFetch;

import PriceFreezeButton from "../pricing/PriceFreezeButton";

describe("PriceFreezeButton", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("renders freeze button with price", async () => {
    // No existing freeze
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: [] }),
    });

    await act(async () => {
      render(
        <PriceFreezeButton
          entityId="FL101"
          entityType="FLIGHT"
          userId="user1"
          currentPrice={5000}
        />
      );
    });

    expect(screen.getByText(/freeze price at ₹5000/i)).toBeInTheDocument();
  });

  it("returns null when no userId", () => {
    const { container } = render(
      <PriceFreezeButton
        entityId="FL101"
        entityType="FLIGHT"
        currentPrice={5000}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  it("shows countdown after successful freeze", async () => {
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    // No existing freeze
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ success: true, data: [] }),
    });

    await act(async () => {
      render(
        <PriceFreezeButton
          entityId="FL101"
          entityType="FLIGHT"
          userId="user1"
          currentPrice={5000}
        />
      );
    });

    // Mock the freeze POST response
    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        success: true,
        data: { id: "freeze1", frozenPrice: 5000, expiresAt, active: true },
      }),
    });

    await user.click(screen.getByText(/freeze price/i));

    await waitFor(() => {
      expect(screen.getByText(/price frozen at ₹5000/i)).toBeInTheDocument();
    });

    // Verify countdown is showing
    expect(screen.getByText(/expires in/i)).toBeInTheDocument();
  });

  it("shows error on freeze failure", async () => {
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ success: true, data: [] }),
    });

    await act(async () => {
      render(
        <PriceFreezeButton
          entityId="FL101"
          entityType="FLIGHT"
          userId="user1"
          currentPrice={5000}
        />
      );
    });

    mockFetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({
        success: false,
        error: { message: "Price already frozen" },
      }),
    });

    await user.click(screen.getByText(/freeze price/i));

    await waitFor(() => {
      expect(screen.getByText(/price already frozen|failed to freeze/i)).toBeInTheDocument();
    });
  });

  it("resets when countdown reaches zero", async () => {
    // Mock an existing freeze that expires in 2 seconds
    const expiresAt = new Date(Date.now() + 2000).toISOString();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        success: true,
        data: [{ id: "freeze1", frozenPrice: 5000, expiresAt, active: true }],
      }),
    });

    await act(async () => {
      render(
        <PriceFreezeButton
          entityId="FL101"
          entityType="FLIGHT"
          userId="user1"
          currentPrice={5000}
        />
      );
    });

    await waitFor(() => {
      expect(screen.getByText(/price frozen/i)).toBeInTheDocument();
    });

    // Advance time past expiry
    act(() => {
      jest.advanceTimersByTime(3000);
    });

    await waitFor(() => {
      expect(screen.getByText(/freeze price/i)).toBeInTheDocument();
    });
  });
});
