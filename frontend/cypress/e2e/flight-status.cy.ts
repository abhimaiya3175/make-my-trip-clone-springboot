/// <reference types="cypress" />

describe("Flight Status Feature", () => {
  it("should search for a flight and display its status", () => {
    // Intercept flight status API
    cy.intercept("GET", "**/api/flight-status/AI101", {
      statusCode: 200,
      body: {
        success: true,
        data: {
          flightId: "AI101",
          airline: "Air India",
          origin: "DEL",
          destination: "BOM",
          scheduledDeparture: "2026-03-06T10:00:00Z",
          estimatedDeparture: "2026-03-06T10:00:00Z",
          status: "ON_TIME",
          delayMinutes: 0,
          delayReason: null,
          lastUpdated: new Date().toISOString(),
        },
        requestId: "req-1",
      },
    }).as("getFlightStatus");

    cy.visit("/flight-status");

    // Page heading
    cy.contains("Live Flight Status Tracker").should("be.visible");

    // Type flight number and submit
    cy.get("input").first().type("AI101");
    cy.contains("Track Flight").click();

    // Wait for API and check status is displayed
    cy.wait("@getFlightStatus");
    cy.contains("AI101").should("be.visible");
    cy.contains("ON_TIME").should("be.visible");
    cy.contains("DEL").should("be.visible");
    cy.contains("BOM").should("be.visible");
  });
});
