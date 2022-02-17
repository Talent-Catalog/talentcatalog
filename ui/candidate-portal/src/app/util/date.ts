/**
 * Converts a date (which will normally include both date and time) and converts it to just a date.
 * <p/>
 * @param date Can be anything that can be converted to a Date - eg a standard date format string.
 */
export function toDateOnly(date: any): Date {
  if (!date) {
    //For null dates, returned the oldest possible date so that they sort to the bottom.
    return new Date(0);
  }
  //Convert the incoming date into a proper Date object
  const origDate = new Date(date);
  //Return a new date just constructed from the incoming dates, year, month and day
  return new Date(Date.UTC(origDate.getFullYear(), origDate.getMonth(), origDate.getDate()));
}
