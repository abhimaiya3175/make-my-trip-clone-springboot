/// <reference types="cypress" />

describe("Seat Selection Feature", () => {
  it("should display the seat map and allow seat selection", () => {
    // Intercept seats API
    cy.intercept("GET", "**/api/seatroom/seats/flight/**", {
      statusCode: 200,
      body: {
        success: true,
        data: [
          { id: "s1", seatNumber: "1A", seatClass: "ECONOMY", price: 500, available: true, locked: false },
          { id: "s2", seatNumber: "1B", seatClass: "ECONOMY", price: 500, available: true, locked: false },
          { id: "s3", seatNumber: "1C", seatClass: "ECONOMY", price: 500, available: false, locked: false },
          { id: "s4", seatNumber: "2A", seatClass: "BUSINESS", price: 1500, available: true, locked: false },
          { id: "s5", seatNumber: "2B", seatClass: "BUSINESS", price: 1500, available: true, locked: true },
        ],
        requestId: "req-1",
      },
    }).as("getSeats");

    // Intercept lock API
    cy.intercept("POST", "**/api/seatroom/seats/*/lock", {
      statusCode: 200,
      body: {
        success: true,
        data: { id: "s1", seatNumber: "1A", locked: true, lockedByUserId: "user-1" },
        requestId: "req-2",
      },
    }).as("lockSeat");

    // Intercept flight details for the booking page
    cy.intercept("GET", "**/api/flight/flight-1", {
      statusCode: 200,
      body: {
        success: true,
        data: {
          id: "flight-1",
          flightname: "AI101",
          from: "Delhi",
          to: "Mumbai",
          price: 5000,
          date: "2026-03-06",
          airline: "Air India",
        },
        requestId: "req-3",
      },
    }).as("getFlight");

    cy.visit("/book-flight/flight-1");

    // Seat map should be rendered
    cy.wait("@getSeats");
    cy.contains("1A").should("be.visible");
    cy.contains("1B").should("be.visible");
  });
});
