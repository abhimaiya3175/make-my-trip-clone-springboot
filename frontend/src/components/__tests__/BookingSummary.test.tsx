import React from "react";
import { render, screen } from "@testing-library/react";
import BookingSummary from "@/components/booking/BookingSummary";

describe("BookingSummary", () => {
  it("renders booking details when booking prop is populated", () => {
    const mockFlightBooking = {
      id: "book-123",
      entityId: "FL-777",
      bookingStatus: "CONFIRMED",
      bookingDate: "2030-01-10",
      quantity: 2,
      totalPrice: 7500,
    };

    render(<BookingSummary booking={mockFlightBooking} type="flight" />);

    expect(screen.getByText("FL-777")).toBeInTheDocument();
    expect(screen.getByText("CONFIRMED")).toBeInTheDocument();
  });

  it("shows loading state when booking is empty", () => {
    render(<BookingSummary booking={null} type="flight" />);

    expect(screen.getByText(/loading booking details/i)).toBeInTheDocument();
  });
});
