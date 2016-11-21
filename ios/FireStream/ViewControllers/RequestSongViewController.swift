//
//  RequestSongViewController.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Firebase

class RequestSongViewController: MaterialViewController, UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet weak var tableView: UITableView!
    
    var songs: [Song] = [Song]()
    
    override var showBtnSearch: Bool {
        get { return true }
        set {}
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.tableFooterView = UIView(frame: CGRect.zero)
        tableView.backgroundColor = UIColor.colorBg
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 80
        tableView.register(UINib(nibName: "SongTableViewCell", bundle: nil), forCellReuseIdentifier: "songCell")
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
        return songs.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "songCell") as! SongTableViewCell
        guard let song = songs[safe: indexPath.row] else { return cell }
        cell.lblName.text = song.name
        cell.lblInfo.text = "\(song.artist)"
        cell.imgAlbumArt.sd_setImage(with: URL(string: song.albumUrl))
        cell.showBtnAdd = true
        cell.btnAdd.isEnabled = true
        cell.btnAdd.tag = indexPath.row
        cell.btnAdd.addTarget(self, action: #selector(btnAddPressed(_:)), for: .touchUpInside)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    }
    
    func btnAddPressed(_ sender: UIButton) {
        sender.isEnabled = false
        guard let song = songs[safe: sender.tag] else { return }
        guard let oldParty = extraDataObject as? Party else { return }
        let partyRef = Utils.getDatabase().reference(fromURL: "https://firestream-4e998.firebaseio.com/parties").child(oldParty.id)
        partyRef.observeSingleEvent(of: .value, with: { (snapshot) in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let newParty = Party(dict: partyDict)
            newParty.requests.append(song)
            partyRef.setValue(newParty.dictionaryValueWithTimestamp)
        })
    }
    
    // MARK: AppBarViewDelegate
    func appBarSearchDidChange(searchText: String) {
        songs.removeAll()
        SpotifyInterface.Search(query: searchText, type: "track", postCommandHandler: { (tempData) in
            guard let data = tempData else { return }
            guard let jsonResult = (try? JSONSerialization.jsonObject(
                with: data,
                options: JSONSerialization.ReadingOptions.mutableContainers)) as? [String:Any] else { return }
            guard let tracks = jsonResult["tracks"] as? [String:Any] else { return }
            guard let items = tracks["items"] as? [[String:Any]] else { return }
            self.songs = items.map { Song(dict: $0) }
            DispatchQueue.main.async {
                self.tableView.reloadData()
            }
        })
    }
}
