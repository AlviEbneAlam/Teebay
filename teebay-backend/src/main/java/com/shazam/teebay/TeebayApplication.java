package com.shazam.teebay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TeebayApplication {


	public static void main(String[] args) {
		try{
			SpringApplication.run(TeebayApplication.class, args);
		}
		catch(Exception ex){
			System.out.println(ex.getLocalizedMessage());
		}

	}

}
