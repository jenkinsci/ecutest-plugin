FROM python:3.9

COPY config/ ./
COPY target/sbom.json ./

RUN pip install pipenv

ENTRYPOINT ["python", "check_dependencies.py", "--allowlist=allowed_licenses.json", "--sbom=sbom.json"]