package TagHelper;

public class Address {
    private final String street;
    private final String city;
    private final String houseNumber;
    private final String postCode;
    private final String country;

    public Address(String street, String houseNumber, String city, String postCode, String country){
        this. street = street;
        this. city = city;
        this. houseNumber = houseNumber;
        this. postCode = postCode;
        this.country = country;

    }
    public String getStreet() {
        return street;

    }
    public String getCity() {
        return city;
    }
    public String getHouseNumber() {
        return houseNumber;
    }
    public String getPostCode() {
        return postCode;

    }
    public String getCountry() {
        return country;

    }

}
