export function generateYearArray(startYear?: number,
                                  reverse?: boolean,
                                  endYear?: number,
                                  endYearOffset?: number): number[] {
  // Set sensible defaults
  const start = startYear || 1950;
  const end = (endYear || new Date().getFullYear()) + (endYearOffset || 0);

  // Generate array in preferred order
  const years = [];
  for (let year = start; year <= end; year++) {
    years.push(year);
  }

  if (reverse) {
    years.reverse();
  }

  return years;
}
