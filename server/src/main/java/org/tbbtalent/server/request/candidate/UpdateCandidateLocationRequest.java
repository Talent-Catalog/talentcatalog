package org.tbbtalent.server.request.candidate;

public class UpdateCandidateLocationRequest {

    private Long countryId;
    private String city;
    private Integer yearOfArrival;

    public Long getCountryId() {
        return countryId;
    }

    public void setCountry(Long countryId) {
        this.countryId = countryId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getYearOfArrival() { return yearOfArrival; }

    public void setYearOfArrival(Integer yearOfArrival) { this.yearOfArrival = yearOfArrival; }


}
