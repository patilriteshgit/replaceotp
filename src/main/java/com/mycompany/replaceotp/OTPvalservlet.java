/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.replaceotp;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.json.JSONObject;

@WebServlet("/valrepotp")
public class OTPvalservlet extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() {
        sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    }

    @Override
    public void destroy() {
        sessionFactory.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //String aadharNumber = request.getParameter("aadharNumber");
        //String otp = request.getParameter("otp");
         // Read the JSON data from the request body
        BufferedReader reader = request.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        reader.close();

        // Parse the JSON data to extract the Aadhar number and OTP
        JSONObject jsonObject = new JSONObject(jsonBody.toString());
        String aadharNumber = jsonObject.getString("aadharNumber");
        String otp = jsonObject.getString("otp");

        boolean isValid = otpIsValid(aadharNumber, otp);

        JSONObject jsonResponse = new JSONObject();

        if (isValid) {
            jsonResponse.put("message", "Authentication successful");

            // Deleting if success auth
            
            // added to delet record
            deleteOTP(aadharNumber);
        } else {
            jsonResponse.put("message", "Authentication failed");
        }

        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    private boolean otpIsValid(String aadharNumber, String otp) {
        String storedOTP = getOTPFromDatabase(aadharNumber);
        return otp.equals(storedOTP);
    }

    private String getOTPFromDatabase(String aadharNumber) {
        Session session = null;
        String storedOTP = null;

        try {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            OTP otpEntity = session.get(OTP.class, aadharNumber);
            if (otpEntity != null) {
                storedOTP = otpEntity.getOtp();
            }

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return storedOTP;
    }

    private void deleteOTP(String aadharNumber) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        OTP otpEntity = session.get(OTP.class, aadharNumber);
        if (otpEntity != null) {
            session.delete(otpEntity);
        }
        
        session.getTransaction().commit();
        session.close();
    }
}
