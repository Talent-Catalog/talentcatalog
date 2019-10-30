export function generateYearArray(startYear: number = 1950,
                                  reverse: boolean = true,
                                  endYear?: number,
                                  endYearOffset: number = 0): number[] {
  // Set sensible defaults
  if (!endYear) {
    endYear = new Date().getFullYear();
  }
  endYear += endYearOffset;

  // Generate array in preferred order
  const years = [];
  for (let year = startYear; year <= endYear; year++) {
    years.push(year);
  }

  if (reverse) {
    years.reverse();
  }

  return years;
}
