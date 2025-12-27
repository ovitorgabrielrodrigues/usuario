package com.javanauta.usuario.business;

import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.Exceptios.ConflictExceptions;
import com.javanauta.usuario.infrastructure.Exceptios.ResourceNotFoundException;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private String email;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExistente(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    /* Metodo verifica se email existe e caso exista ele gera uma Excessão */
    public void emailExistente(String email) {
        try {
            boolean existente = verificaEmailExistente(email);
            if (existente) {
                throw new ConflictExceptions("Email já cadastrado!" + email);
            }
        } catch (ConflictExceptions e) {
            throw new CompletionException("Email já cadastrado! ", e.getCause());
        }
    }

    /* Metodo responsável apenas por chamar a função existsByEmail na repository */
    public boolean verificaEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    /* Buscar um usuario, se ele não existir lançar uma excessão.
     Se ele existir a excessão nem é criada! () -> é uma lambda */

    public Usuario buscaUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado!" + email)
        );
    }

    public void deletaUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto) {
        /* Método que busca um email do usuario através do token
        (Tira a obrigatoriedade de passar um Email)*/
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        // Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        // Buscar os dados do usuário no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não localizado!"));

        // União dos dados que foram recebidos da requisição DTO com os dados do banco de dados.
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        // Salva os dados convertidos do usuário e retorna um UsuarioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }
}
