//
//  PartyViewController.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Firebase

class PartyViewController: MaterialViewController, UITableViewDelegate, UITableViewDataSource {
    @IBOutlet weak var lblCurrentSong: UILabel!
    @IBOutlet weak var lblCurrentArtist: UILabel!
    @IBOutlet weak var imgCurrentAlbumArt: UIImageView!
    @IBOutlet weak var tableView: UITableView!
    
    var party: Party! {
        didSet {
            (navigationController as? MaterialNavigationController)?.defaultAppBarView.title = party.name
            
            if let firstSong = party.queue.first {
                lblCurrentSong.text = firstSong.name
                lblCurrentArtist.text = firstSong.artist
                imgCurrentAlbumArt.sd_setImage(with: URL(string: firstSong.albumUrl))
            }
            tableView.reloadData()
        }
    }
    
    override var showAppBarShadow: Bool {
        get { return false }
        set { }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.tableFooterView = UIView(frame: CGRect.zero)
        tableView.backgroundColor = UIColor.colorBg
        tableView.register(UINib(nibName: "SongTableViewCell", bundle: nil), forCellReuseIdentifier: "songCell")
        
        if let tempParty = extraDataObject as? Party {
            party = tempParty
        } else {
            _ = self.navigationController?.popViewController(animated: true)
            return
        }
        
    }
    
    func subscribeToParty() {
        let partyRef = Utils.getDatabase().reference(fromURL: "https://firestream-4e998.firebaseio.com/parties").child(party.id)
        
        partyRef.observe(.value, with: { snapshot in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let newParty = Party(dict: partyDict)
            self.party = newParty
        })
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    // MARK: UITableViewDataSource
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if party == nil {
            return 0
        }
        return party.queue.count - 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "songCell") as! SongTableViewCell
        guard let song = party.queue[safe: indexPath.row + 1] else { return cell }
        cell.lblName.text = song.name
        cell.lblInfo.text = "\(song.artist)|\(song.duration)"
        cell.imgAlbumArt.sd_setImage(with: URL(string: song.albumUrl))
        return cell
    }
}
