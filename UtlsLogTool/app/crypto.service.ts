import {Injectable} from "@angular/core";
import {AppConstants} from "./app.constants";

let nodeRSA = require('node-rsa');
let crypto = require("crypto");

@Injectable()
export class CryptoService {

    constructor() {
    }

    public doDecryptContent(encryptedContent: string): string{
        if(encryptedContent){
            let encryptedData = new Buffer(encryptedContent, "base64").toString("binary");
            let decipher = crypto.createDecipher('aes-128-ecb', AppConstants.CRYPTO_TOOL_NAME);
            let decoded = decipher.update(encryptedData, 'binary', 'utf8');
            decoded += decipher.final('utf8');
            return decoded;
        }
        return undefined;
    }

    public doEncryptContent(clearText: string): string{
        let encipher = crypto.createCipher('aes-128-ecb', AppConstants.CRYPTO_TOOL_NAME);
        let encryptedData = encipher.update(clearText, 'utf8', 'binary');
        encryptedData += encipher.final('binary');
        let encodedEncryptedData = new Buffer(encryptedData, 'binary').toString('base64');
        return encodedEncryptedData;
    }




}
