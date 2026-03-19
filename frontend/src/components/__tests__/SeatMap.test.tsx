import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock global fetch
const mockFetch = jest.fn();
global.fetch = mockFetch;

import SeatMap from "../Flights/SeatMap";

const mockSeats = [
  {
    id: "s1",
    flightId: "FL101",
    seatNumber: "1A",
    row: "1",
    column: "A",
    seatClass: "ECONOMY",
    available: true,
    basePrice: 5000,
    premiumSurcharge: 0,
    effectivePrice: 5000,
    locked: false,
    lockedByMe: false,
  },
  {
    id: "s2",
    flightId: "FL101",
    seatNumber: "1B",
    row: "1",
    column: "B",
    seatClass: "ECONOMY",
    available: false,
    basePrice: 5000,
    premiumSurcharge: 0,
    effectivePrice: 5000,
    locked: false,
    lockedByMe: false,
  },
  {
    id: "s3",
    flightId: "FL101",
    seatNumber: "1C",
    row: "1",
    column: "C",
    seatClass: "BUSINESS",
    available: true,
    basePrice: 5000,
    premiumSurcharge: 2000,
    effectivePrice: 7000,
    locked: false,
    lockedByMe: false,
  },
];

describe("SeatMap", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders seat grid with correct states", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: mockSeats }),
    });

    render(<SeatMap flightId="FL101" userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("1A")).toBeInTheDocument();
    });

    expect(screen.getByText("1B")).toBeInTheDocument();
    expect(screen.getByText("1C")).toBeInTheDocument();
  });

  it("shows loading state", () => {
    mockFetch.mockImplementation(() => new Promise(() => {}));

    render(<SeatMap flightId="FL101" />);
    expect(screen.getByText(/loading seat/i)).toBeInTheDocument();
  });

  it("shows error when seat fetch fails", async () => {
    mockFetch.mockRejectedValue(new Error("Network error"));

    render(<SeatMap flightId="FL101" />);

    await waitFor(() => {
      expect(screen.getByText(/failed to load seat map/i)).toBeInTheDocument();
    });
  });

  it("clicking available seat triggers lock API", async () => {
    const user = userEvent.setup();
    const onSeatSelect = jest.fn();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: mockSeats }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: { ...mockSeats[0], locked: true, lockedByMe: true } }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: mockSeats }),
      });

    render(<SeatMap flightId="FL101" userId="user1" onSeatSelect={onSeatSelect} />);

    await waitFor(() => {
      expect(screen.getByText("1A")).toBeInTheDocument();
    });

    await user.click(screen.getByText("1A"));

    await waitFor(() => {
      // Verify the lock API was called
      const lockCall = mockFetch.mock.calls.find(
        (call) => typeof call[0] === "string" && call[0].includes("/lock")
      );
      expect(lockCall).toBeDefined();
    });
  });

  it("shows conflict error on 409 response", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: mockSeats }),
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 409,
        json: async () => ({
          success: false,
          error: { message: "Seat already taken by another passenger" },
        }),
      });

    render(<SeatMap flightId="FL101" userId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("1A")).toBeInTheDocument();
    });

    await user.click(screen.getByText("1A"));

    await waitFor(() => {
      expect(
        screen.getByText(/seat already taken|failed to lock seat/i)
      ).toBeInTheDocument();
    });
  });
});
