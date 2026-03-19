import React from "react";
import { render, screen, waitFor, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock next/router
jest.mock("next/router", () => ({
  useRouter: () => ({ query: {} }),
}));

// Mock flightStatusService
const mockGetFlightStatus = jest.fn();
jest.mock("@/services/flightStatusService", () => ({
  getFlightStatus: (...args: any[]) => mockGetFlightStatus(...args),
}));

import FlightStatusTracker from "../Flights/FlightStatusTracker";

const mockStatusOnTime = {
  flightId: "AI101",
  airline: "Air India",
  origin: "DEL",
  destination: "BOM",
  scheduledDeparture: "2026-03-06T10:00:00Z",
  estimatedDeparture: "2026-03-06T10:00:00Z",
  status: "ON_TIME",
  delayMinutes: 0,
  delayReason: null,
  lastUpdated: "2026-03-06T09:30:00Z",
};

const mockStatusDelayed = {
  ...mockStatusOnTime,
  status: "DELAYED",
  delayMinutes: 45,
  delayReason: "Weather conditions",
  estimatedDeparture: "2026-03-06T10:45:00Z",
};

const mockStatusCancelled = {
  ...mockStatusOnTime,
  status: "CANCELLED",
};

const mockStatusBoarding = {
  ...mockStatusOnTime,
  status: "BOARDING",
};

describe("FlightStatusTracker", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("renders search input and track button", () => {
    render(<FlightStatusTracker />);
    expect(screen.getByPlaceholderText(/enter flight number/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /track/i })).toBeInTheDocument();
  });

  it("renders ON_TIME status with green badge", async () => {
    mockGetFlightStatus.mockResolvedValue(mockStatusOnTime);
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    render(<FlightStatusTracker />);
    const input = screen.getByPlaceholderText(/enter flight number/i);
    await user.type(input, "AI101");
    await user.click(screen.getByRole("button", { name: /track/i }));

    await waitFor(() => {
      expect(screen.getByText(/ON TIME/)).toBeInTheDocument();
    });
    expect(screen.getByText("DEL")).toBeInTheDocument();
    expect(screen.getByText("BOM")).toBeInTheDocument();
  });

  it("renders DELAYED status with delay reason", async () => {
    mockGetFlightStatus.mockResolvedValue(mockStatusDelayed);
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    render(<FlightStatusTracker />);
    const input = screen.getByPlaceholderText(/enter flight number/i);
    await user.type(input, "AI101");
    await user.click(screen.getByRole("button", { name: /track/i }));

    await waitFor(() => {
      expect(screen.getByText(/DELAYED/)).toBeInTheDocument();
    });
    expect(screen.getByText("45 minutes")).toBeInTheDocument();
    expect(screen.getByText("Weather conditions")).toBeInTheDocument();
  });

  it("renders CANCELLED status", async () => {
    mockGetFlightStatus.mockResolvedValue(mockStatusCancelled);
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    render(<FlightStatusTracker />);
    const input = screen.getByPlaceholderText(/enter flight number/i);
    await user.type(input, "AI101");
    await user.click(screen.getByRole("button", { name: /track/i }));

    await waitFor(() => {
      expect(screen.getByText(/CANCELLED/)).toBeInTheDocument();
    });
  });

  it("renders BOARDING status", async () => {
    mockGetFlightStatus.mockResolvedValue(mockStatusBoarding);
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    render(<FlightStatusTracker />);
    const input = screen.getByPlaceholderText(/enter flight number/i);
    await user.type(input, "AI101");
    await user.click(screen.getByRole("button", { name: /track/i }));

    await waitFor(() => {
      expect(screen.getByText(/BOARDING/)).toBeInTheDocument();
    });
  });

  it("shows error on fetch failure", async () => {
    mockGetFlightStatus.mockRejectedValue({
      response: { data: { message: "Flight not found" } },
    });
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    render(<FlightStatusTracker />);
    const input = screen.getByPlaceholderText(/enter flight number/i);
    await user.type(input, "INVALID");
    await user.click(screen.getByRole("button", { name: /track/i }));

    await waitFor(() => {
      expect(screen.getByText("Flight not found")).toBeInTheDocument();
    });
  });

  it("polls status every 30 seconds", async () => {
    mockGetFlightStatus.mockResolvedValue(mockStatusOnTime);
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

    render(<FlightStatusTracker />);
    const input = screen.getByPlaceholderText(/enter flight number/i);
    await user.type(input, "AI101");
    await user.click(screen.getByRole("button", { name: /track/i }));

    await waitFor(() => {
      expect(screen.getByText(/ON TIME/)).toBeInTheDocument();
    });

    expect(mockGetFlightStatus).toHaveBeenCalledTimes(1);

    // Advance 30 seconds to trigger poll
    act(() => {
      jest.advanceTimersByTime(30000);
    });

    await waitFor(() => {
      expect(mockGetFlightStatus).toHaveBeenCalledTimes(2);
    });
  });
});
