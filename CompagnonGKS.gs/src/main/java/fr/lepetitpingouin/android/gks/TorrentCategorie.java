package fr.lepetitpingouin.android.gks;

/**
 * Created by gregory on 02/10/13.
 */
public class TorrentCategorie {

    public TorrentCategorie() {

    }

    public int getIcon(String catCode) {
        String codeList;

        codeList = "|3|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_windows;

        codeList = "|4|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_apple;

        codeList = "|5|6|19|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_dvd;

        codeList = "|7|8|10|11|12|20|21|22|23|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_tv;

        codeList = "|9|13|14|15|16|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_tvhd;

        codeList = "|17|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_bluray;

        codeList = "|29|30|31|32|34|38|";
        if(codeList.contains("|"+catCode+"|"))
            return R.drawable.ic_cat_console;

        return R.drawable.ic_cat_gks;
    }
}

/*
<option value="3">Windows </option>
<option value="4">Mac </option>
<option value="5">DVDRip/BDRip </option>
<option value="6">DVDRip/BDRip VOSTFR </option>
<option value="7">Emissions TV </option>
<option value="8">Docs </option>
<option value="9">Docs HD </option>
<option value="10">TV PACK </option>
<option value="11">TV VOSTFR </option>
<option value="12">TV VF </option>
<option value="13">TV HD VOSTFR </option>
<option value="14">TV HD VF </option
><option value="15">HD 720p </option
><option value="16">HD 1080p </option
><option value="17">Full BluRay </option
><option value="18">Divers </option><
option value="19">DVDR </option><option
value="20">DVDR Series </option
><option value="21">Anime </option
><option value="22">TV VO </option>
<option value="23">Concerts </option>
<option value="24">eBooks </option>
<option value="28">Sport </option><
option value="29">PC Games </option>
<option value="30">Nintendo DS </option
><option value="31">Wii </option
><option value="32">Xbox 360 </option
><option value="34">PSP </option><
option value="38">PSX/PS2/PS3 </option
><option value="39">Flac </option>
 */