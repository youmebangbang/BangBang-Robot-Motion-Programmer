
class serialBufferClass
{
  private:
    char serialBuffer[2048];
    int serialSize;
    long bufPos;

  public:
    serialBufferClass();
    char* readUntilDelim();

};
