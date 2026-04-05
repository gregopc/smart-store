package com.example.smartstore.dto; 

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;  
import jakarta.validation.constraints.NotBlank; 
import jakarta.validation.constraints.NotNull; 
import jakarta.validation.constraints.Size; 
import lombok.*;

import java.math.BigDecimal; 

@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class ProductRequest { 
    
    @NotBlank 
    @Size(min = 3) 
    private String name; 
    
    private String description; 
    
    @NotNull 
    @DecimalMin(value = "0.0", inclusive = false) 
    private BigDecimal price; 
    
    private String category; 
    
    @NotNull 
    @Min(0) 
    private Integer stock; 
}