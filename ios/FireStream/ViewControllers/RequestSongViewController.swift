//
//  RequestSongViewController.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit

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
        guard let song = songs[safe: indexPath.row + 1] else { return cell }
        cell.lblName.text = song.name
        cell.lblInfo.text = "\(song.artist)|\(TimeInterval(song.duration.msToSeconds).minuteSecond)"
        cell.imgAlbumArt.sd_setImage(with: URL(string: song.albumUrl))
        cell.showBtnAdd = true
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    }
    
    // MARK: AppBarViewDelegate
    func appBarSearchDidChange(searchText: String) {
        
    }
}
