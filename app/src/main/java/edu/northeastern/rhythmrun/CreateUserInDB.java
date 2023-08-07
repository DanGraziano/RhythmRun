package edu.northeastern.rhythmrun;

public class CreateUserInDB {

    private String firstname;
    private String lastname;
    private String age;
    private String weight;
    private String height;
    private String email;
    private String password;
    private String cadenceGoal;

    // for firebase
    public CreateUserInDB(){

    }

    public CreateUserInDB(String firstname, String lastname, String age, String height, String weight, String email, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.email = email;
        this.password = password;
        this.cadenceGoal = "Beginner";
    }

    public CreateUserInDB(String firstname, String lastname, String age, String height, String weight, String email, String password, String cadenceGoal) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.email = email;
        this.password = password;
        this.cadenceGoal = cadenceGoal;
    }

    // Getters for accessing the fields
    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getAge() {
        return age;
    }

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCadenceGoal() {
        return cadenceGoal;
    }

}
