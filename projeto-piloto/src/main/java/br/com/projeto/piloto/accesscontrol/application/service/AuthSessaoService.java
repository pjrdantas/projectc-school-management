package br.com.projeto.piloto.accesscontrol.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthSessao;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.repository.AuthSessaoRepository;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthSessaoModel;
import br.com.projeto.piloto.accesscontrol.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthSessaoService {

    private final AuthSessaoRepository authSessaoRepository;
    private final JwtUtil jwtUtil;

    
    @Transactional
    public AuthSessaoModel criarSessao(Long authUsuarioId, String token, String refreshToken) {

        authSessaoRepository.invalidarTodasSessoes(authUsuarioId);

        authSessaoRepository.deletarSessoesInativas(authUsuarioId);

        LocalDateTime dataExpiracao = jwtUtil.extractExpiration(token);

        AuthSessao sessao = AuthSessao.builder()
                .authUsuarioId(authUsuarioId)
                .token(token)
                .refreshToken(refreshToken)
                .dataCriacao(LocalDateTime.now())
                .dataExpiracao(dataExpiracao)
                .ativo("S")
                .build();

        @SuppressWarnings("null")
		AuthSessao sessaoSalva = authSessaoRepository.save(sessao);
        return toModel(sessaoSalva);
    }

   
    public boolean validarSessao(String token) {
        LocalDateTime agora = LocalDateTime.now();
        Optional<AuthSessao> sessao = authSessaoRepository.findByTokenAndAtivoAndDataExpiracaoAfter(token, "S", agora);
        return sessao.isPresent();
    }

    public Optional<AuthSessaoModel> encontrarPorRefreshToken(String refreshToken) {
        return authSessaoRepository.findByRefreshToken(refreshToken)
                .filter(AuthSessao::isAtiva)
                .filter(s -> !s.isExpirada())
                .map(this::toModel);
    }

    @Transactional
    public void invalidarSessao(Long sessaoId) {
        authSessaoRepository.invalidarSessao(sessaoId);
    }

    @Transactional
    public void invalidarTodasSessoes(Long authUsuarioId) {
        authSessaoRepository.invalidarTodasSessoes(authUsuarioId);
    }

    private AuthSessaoModel toModel(AuthSessao sessao) {
        return AuthSessaoModel.builder()
                .id(sessao.getId())
                .authUsuarioId(sessao.getAuthUsuarioId())
                .token(sessao.getToken())
                .refreshToken(sessao.getRefreshToken())
                .dataCriacao(sessao.getDataCriacao())
                .dataExpiracao(sessao.getDataExpiracao())
                .ativo(sessao.getAtivo())
                .build();
    }
}
