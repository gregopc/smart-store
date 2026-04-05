package com.example.smartstore.domain; 

import lombok.*; 
import org.hibernate.annotations.GenericGenerator; 

import jakarta.persistence.*; 
import java.math.BigDecimal; 
import java.time.LocalDateTime; 
import java.util.UUID; 

@Entity 
@Table(name = "products") 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Product {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false) 
    private UUID id; 
    
    @Column(nullable = false) 
    private String name; 
    
    @Column(columnDefinition = "text") 
    private String description; 
    
    @Column(nullable = false) 
    private BigDecimal price; 
    
    private String category; 
    
    @Column(nullable = false) 
    private Integer stock; 
    
    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; 
    
    @PrePersist 
    public void prePersist() { 
        this.createdAt = LocalDateTime.now(); 
    } 
}