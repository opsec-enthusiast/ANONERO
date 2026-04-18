# ANONERO
Privacy and security focused Monero wallet with advanced features in a slick UX.

### QUICKSTART
- Download the APK for the most current release [here](http://git.anonero5wmhraxqsvzq2ncgptq6gq45qoto6fnkfwughfl4gbt44swad.onion/ANONERO/ANONERO/releases) and install it

### DISCLAIMER
Be sure to back up your wallet recovery seed AND passphrase. 

ANON enforces passphras encryption on all seeds!

We are NOT responsible for lost or stolen funds.

### MAIN FEATURES
- Monero only
- Mandatory proxy
- No 3rd-party services
- Polyseed mnemonic
- Passphrase encryption
- No subaddress reuse
- Encrypted backups
- UTXO management
- Secure view-key syncing
- Airgapped transactions


### HOW TO BUILD

1. Clone ANONERO repo: `git -c http.proxy=socks5h://127.0.0.1:9050 clone http://git.anonero5wmhraxqsvzq2ncgptq6gq45qoto6fnkfwughfl4gbt44swad.onion/ANONERO/ANONERO.git`

2. Clone Monero repo: `git -c http.proxy=socks5h://127.0.0.1:9050 clone http://git.anonero5wmhraxqsvzq2ncgptq6gq45qoto6fnkfwughfl4gbt44swad.onion/ANONERO/monero.git`

3. Link external libs: `ln -s ~/monero ~/ANONERO/external-libs/monero`

4. Update submodules: `cd monero && git submodule update --init --force`

5. Build external libs: `cd ~/ANONERO/external-libs && sudo make`

Then, fire up Android Studio and build the APK.

### Donations
- Address: `8BQFYQTDMr9ibTsi3QMutG4EW3Gwv9a8N1XRLV95QBrg5THWSAt8no6GKgXErgEYzAUMiEoqZ6zHYUewj27bmvRD7JBCGmf`


