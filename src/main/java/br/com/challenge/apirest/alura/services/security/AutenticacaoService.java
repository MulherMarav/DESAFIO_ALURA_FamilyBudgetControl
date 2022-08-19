package br.com.challenge.apirest.alura.services.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.challenge.apirest.alura.repositories.security.UsuarioRepository;
import br.com.challenge.apirest.alura.security.jwt.JwtTokenProvider;
import br.com.challenge.apirest.alura.vo.security.TokenVO;
import br.com.challenge.apirest.alura.vo.security.UsuarioCredenciais;

@Service
public class AutenticacaoService {
	
	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private UsuarioRepository repository;
	

	public ResponseEntity<?> signin(UsuarioCredenciais data) {

		if (checkIfParamsIsNotNull(data))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solicitação de cliente inválida!");

		var tokenResponse = new TokenVO();

		try {

			var email = data.getEmail();
			var senha = data.getSenha();
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, senha);
			authManager.authenticate(authentication);

			var user = repository.findByEmail(email);

			if (user.isPresent()) {
				tokenResponse = tokenProvider.createAccessToken(email, user.get().getRoles());
			} else {
				throw new UsernameNotFoundException("Email " + email + " não encontrado!");
			}

		} catch (Exception e) {
			throw new BadCredentialsException("Email ou senha inválidos!");
		}

		if (tokenResponse == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solicitação de cliente inválida!");
		else
			return ResponseEntity.ok(tokenResponse);
	}

	public ResponseEntity<?> refreshToken(String email, String refreshToken) {

		if (checkIfParamsIsNotNull(email, refreshToken))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solicitação de cliente inválida!");

		var user = repository.findByEmail(email);

		var tokenResponse = new TokenVO();

		if (user != null) {
			tokenResponse = tokenProvider.refreshToken(refreshToken);
		} else {
			throw new UsernameNotFoundException("Email " + email + " não encontrado!");
		}

		if(tokenResponse == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solicitação de cliente inválida!");
		else
			return ResponseEntity.ok(tokenResponse);
	}

	private boolean checkIfParamsIsNotNull(String email, String refreshToken) {
		return refreshToken == null || refreshToken.isBlank() || email == null || email.isBlank();
	}

	private boolean checkIfParamsIsNotNull(UsuarioCredenciais data) {
		return data == null || data.getEmail() == null || data.getEmail().isBlank() || data.getSenha() == null
				|| data.getSenha().isBlank();
	}
}
