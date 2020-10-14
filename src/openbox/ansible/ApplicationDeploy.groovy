package openbox.ansible;

class ApplicationDeploy
{
    static String STEP_BUILD_ANSIBLE = "docker build --rm --no-cache ";
    static String ANSIBLE_KEY_FILE = "rsa.key";
    static String DOCKER_BUILD_CTX = "/var/jenkins_home/tmp";
    static String DOCKER_BUILD_ARG = "--build-arg ";
    static String DOCKER_BUILD_IMG = "ansible-docker:latest";

    static String STEP_RUN_ANSIBLE = "docker run ";
    static String DOCKER_RUN_CMD = "ansible-playbook ecs.deploy.playbook.yml";

    private String cmd_build;
    private String cmd_run;

    public ApplicationDeploy(
        String awsAccessKey,
        String awsSecretKey,
        String ecrPassword,
        String service,
        String image
    ) {

        this.cmd_build = DockerStepAssembler.assembleDockerBuild(
            DOCKER_BUILD_IMG,
            ["ANSIBLE_SSH_PRIVATE_KEY_FILE": ApplicationDeploy.ANSIBLE_KEY_FILE],
            DOCKER_BUILD_CTX
        );

        this.cmd_run = DockerStepAssembler.assembleDockerRun(
            DOCKER_BUILD_IMG,
            [],
            DOCKER_RUN_CMD + ' --extra-vars: "{' + 
            "ec2_access_key: " + awsAccessKey +
            "ec2_secret_key: " + awsSecretKey +
            "ecr_password: " +  ecrPassword +
            "app_service: " + service +
            "app_image: " + image + '}"'
        );
    }

    public String getBuildCmd()
    {
        return this.cmd_build;
    }

    public String getRunCmd()
    {
        return this.cmd_run;
    }
}