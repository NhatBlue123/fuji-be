package com.example.fuji.controller;

public class JwtService {
    public static final String SECRET = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";

    // public String generateToken(String email) { // Use email as username
    //     Map<String, Object> claims = new HashMap<>();
    //     return createToken(claims, email);
    // }

    // private String createToken(Map<String, Object> claims, String email) {
    //     return Jwts.builder()
    //             .claims(claims)
    //             (email)
    //             .setIssuedAt(new Date())
    //             .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
    //             .signWith(getSignKey(), SignatureAlgorithm.HS256)
    //             .compact();
    // }

}
