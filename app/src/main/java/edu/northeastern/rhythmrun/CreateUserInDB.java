package edu.northeastern.rhythmrun;

public class CreateUserInDB {

    //TODO make private and use getters and setters

    public String firstname;

    public String lastname;

    public String age;

    public String weight;

    public String height;
    public String email;
    public String password;

    public String cadenceGoal;


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
}
