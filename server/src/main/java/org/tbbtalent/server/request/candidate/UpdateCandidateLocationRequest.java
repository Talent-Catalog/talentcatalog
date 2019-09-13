package org.tbbtalent.server.request.candidate;

public class UpdateCandidateLocationRequest {

    private String country;
    private String city;
    private String yearOfArrival;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYearOfArrival() { return yearOfArrival; }

    public void setYearOfArrival(String yearOfArrival) { this.yearOfArrival = yearOfArrival; }


}
