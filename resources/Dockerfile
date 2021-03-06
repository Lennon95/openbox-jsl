############################################################
# Este Dockerfile foi escrito em multiplos estágios porque #
# a chave privada de acesso para clonagem do repositorio é #
# passada via CLI --build-arg ao construir a imagem.       #
# A imagem deve sempre ser construida com o parâmetro --rm #
# para que os containers intermediários sejam removidos    #
############################################################

######## ESTÁGIO INICIAL ########
# instala os pacotes necessários
# para clonar o repositório privado
FROM python:3.6-alpine AS intermediate

RUN apk add --update git openssh-client sshpass

# Copia chaves necessarias para clonar o repositório aws-ansible
ARG ANSIBLE_SSH_PRIVATE_KEY_FILE
RUN mkdir /root/.ssh/
COPY ${ANSIBLE_SSH_PRIVATE_KEY_FILE} /root/.ssh/github_rsa

# Corrige as permissões
RUN chmod 700 /root/.ssh && chmod 600 /root/.ssh/github_rsa

# Adiciona entrada no ssh_config para buscar a chave privada correta
RUN echo -e "Host * \n\
    HostName github.com \n\
    IdentityFile /root/.ssh/github_rsa" > /etc/ssh/ssh_config

# Adiciona github como known_host global
RUN ssh-keyscan -Ht rsa github.com >> /etc/ssh/ssh_known_hosts

# Adiciona chave na keychain e
# clona repositório do playbook de deploy na AWS
RUN eval "$(ssh-agent)" \
    ssh-add -k /root/.ssh/github_rsa; \
    git clone git@github.com:Schaidhauer/aws-ansible.git /root/ansible/ &&\
    chmod 600 /root/ansible/ssh/* &&\
    chmod 700 /root/ansible/ssh &&\
    chmod 644 /root/ansible/ssh/*.pub

######## ESTÁGIO FINAL ########
# Configura o ambiente para executar o Ansible
FROM python:3.6-alpine

# Instala dependencias
RUN apk add --update build-base python-dev py-pip jpeg-dev zlib-dev libffi-dev openssl-dev openssh-client sshpass

# Atualiza pip
RUN pip install --upgrade pip

# Altera LIBRARY_PATH devido a um erro ao buildar zlib
ENV LIBRARY_PATH=/lib:/usr/lib

# Define novo diretorio de trabalho
WORKDIR /ansible

# Define volumes
VOLUME [ "/ansible" ]

# Copia conteudo do container intermediario (clone do repositorio) para o volume
# e restaura as permissões necessárias
COPY --from=intermediate /root/ansible /ansible

# Instala dependencias do Ansible
RUN pip install boto botocore boto3

# Instala o Ansible
RUN pip install ansible==2.9

# Define arquivo de hosts conhecidos para o SSH
RUN echo "UserKnownHostsFile /ansible/ssh/known_hosts" >> /etc/ssh/ssh_config

# Copia chave do repositório
ARG REPO_SSH_PRIVATE_KEY_FILE
COPY ${REPO_SSH_PRIVATE_KEY_FILE} /ansible/ssh/${REPO_SSH_PRIVATE_KEY_FILE}
RUN chmod 600 /ansible/ssh/${REPO_SSH_PRIVATE_KEY_FILE}

# Copia chave SSL
ARG SSL_PRIVATE_KEY_FILE
COPY ${SSL_PRIVATE_KEY_FILE} /ansible/ssh/account.key
RUN chmod 600 /ansible/ssh/account.key
