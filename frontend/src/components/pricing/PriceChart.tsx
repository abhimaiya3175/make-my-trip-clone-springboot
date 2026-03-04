import React from "react";

interface PriceChartProps {
  itemId?: string;
  itemType?: string;
}

const PriceChart: React.FC<PriceChartProps> = ({ itemId, itemType }) => {
  return (
    <div className="p-4">
      <h3 className="font-semibold text-lg mb-2">Price History</h3>
      <p className="text-gray-500 text-sm">
        Dynamic pricing chart for {itemType}: {itemId}
      </p>
      {/* Chart.js price visualization will be rendered here */}
    </div>
  );
};

export default PriceChart;
