INSERT INTO parentesco (id_parentesco, codigo, descricao)
SELECT CAST('00000000-0000-0000-0000-000000000001' AS UUID), 'RESPONSAVEL_LEGAL', 'Responsavel legal'
WHERE NOT EXISTS (
    SELECT 1
    FROM parentesco
    WHERE codigo = 'RESPONSAVEL_LEGAL'
);
