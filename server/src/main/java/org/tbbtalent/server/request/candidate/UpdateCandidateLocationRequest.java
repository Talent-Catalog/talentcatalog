package org.tbbtalent.server.request.candidate;

public class UpdateCandidateLocationRequest {

    private Long country;
    private String city;
    private Integer yearOfArrival;

    public Long getCountry() {
        return country;
    }

    public void setCountry(Long country) {
        this.country = country;
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
