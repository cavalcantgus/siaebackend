package com.siae.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produtores")
@Getter
@Setter
@NoArgsConstructor
public class Produtor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE)
	private Long id;
	
	private String nome;
	private String email;
	
	@Temporal(TemporalType.DATE)
	@JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dataNascimento;
	
	private String endereco;

	@Column(unique = true, nullable = false)
	private String cpf;

	private String rg;
	private String cep;
	private String contato;
	private String municipio;
	private String estado;
	private String banco;
	private String agencia;
	private String conta;
	private String caf;
	
	@Temporal(TemporalType.DATE)
	@JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate validadeCaf;
	
	private String tipoConta;
	private String escolaridade;
	private String estadoCivil;
	private String sexo;
	
	@OneToMany(mappedBy = "produtor", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	@JsonManagedReference
	private List<Documento> documentos = new ArrayList<>();
}
