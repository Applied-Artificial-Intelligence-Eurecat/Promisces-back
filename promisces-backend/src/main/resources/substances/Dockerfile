FROM python:3.9

WORKDIR /app
COPY . /app

RUN pip install requests
RUN pip install tqdm

CMD ["python", "upload_compounds_api.py"]
