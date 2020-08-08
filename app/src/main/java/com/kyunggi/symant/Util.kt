package com.kyunggi.symant

fun String.normalize(): String {
    var w = this
    w = w.replace("[^a-zA-Z]".toRegex(), "")
    return w.toLowerCase()
}

fun String.isEnglish(): Boolean {
    /*
    int len=word.length();
    int digits=0;
    for (int i=0;i < len;++i)
    {
        char c=word.charAt(i);
        if (Character.isDigit(c))
        {
            digits++;
        }
        else if (!Character.isLetter((c)))
        {
            if (new String("-.,!?;_~*#%()\"<>:&'/[]${}+=￦※").indexOf(c) == -1)
            {
                return false;
            }
        }
    }
    if (digits == len)return false;
    */
    //return word.matches("")
    return true
}

fun String.toQueryStringSynonym(): String {
    return "https://tuna.thesaurus.com/pageData/$this"
}

fun String.toQueryStringAntonym(): String {
    return "https://wordsapiv1.p.mashape.com/words/$this/antonyms"
}