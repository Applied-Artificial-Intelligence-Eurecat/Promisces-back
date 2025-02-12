FROM python:3.9.3 as python
RUN apt-get update
RUN apt-get install software-properties-common python3-pip -y
RUN python3 -m pip install --upgrade pip

# RUN apt-get install python-rdkit librdkit1 rdkit-data -y

COPY requirements.txt /modelS/requirements.txt
RUN pip3 install -r /modelS/requirements.txt
COPY . /modelS
WORKDIR /modelS
CMD ["python3", "modelService.py"]
