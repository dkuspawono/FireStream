//
//  PartySearchViewController.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Firebase
import SDWebImage
import Spotify

class PartySearchViewController: MaterialViewController, UITableViewDelegate, UITableViewDataSource {
    
    let FIREBASE_DATABASE_TABLE_PARTIES = "parites"
    
    @IBOutlet weak var tableView: UITableView!
    
    @IBAction func BtnAddPressed() {
        if SpotifyInterface.IsInitilized() {
            ControllerInterface.DoSegue(segueCommand: .ToCreateParty, viewController: self, segueType: .Modal)
        } else {
            guard let controller = SpotifyInterface.Authenticate() else {
                ControllerInterface.DoSegue(segueCommand: .ToCreateParty, viewController: self, segueType: .Modal)
                return
            }
            self.present(controller, animated: true, completion: nil)
        }
    }
    
    var parties: [Party] = [Party]()
    var filteredParties: [Party] = [Party]()
    
    lazy var passwordPopUp: PasswordPopUp = {
        return Bundle.main.loadNibNamed(
            "PasswordPopUp",
            owner: nil,
            options: nil)![0] as! PasswordPopUp
    }()
    
    override var showBtnSearch: Bool {
        get { return true }
        set { }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        (self.navigationController as? MaterialNavigationController)?.updateAppBar()
        tableView.tableFooterView = UIView(frame: CGRect.zero)
        tableView.backgroundColor = UIColor.colorBg
        tableView.register(UINib(nibName: "PartyTableViewCell", bundle: nil), forCellReuseIdentifier: "partyCell")
        
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 90
        
        subscribeToParties()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    // MARK: Fetch Data
    func subscribeToParties() {
        parties.removeAll()
        
        let db = Utils.getDatabase().reference(fromURL: "https://firestream-4e998.firebaseio.com/parties")
        let q = db.queryOrdered(byChild: "attendees").queryLimited(toLast: 50)
        
        q.observe(.childAdded, with: { snapshot in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let party = Party(dict: partyDict)
            self.parties.append(party)
            self.displayParties()
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
        q.observe(.childChanged, with: { (snapshot) in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let party = Party(dict: partyDict)
            for i in 0..<self.parties.count {
                if self.parties[i].id == party.id && !party.compareWith(otherParty: self.parties[i]) {
                    self.parties[i] = party
                    self.displayParties()
                    break
                }
            }
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
    
        q.observe(.childRemoved, with: { (snapshot) in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let party = Party(dict: partyDict)
            for i in 0..<self.parties.count {
                if self.parties[i].id == party.id {
                    self.parties.remove(at: i)
                    self.displayParties()
                    break
                }
            }
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
        
        q.observe(.childMoved, with: { (snapshot) in
            
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
    }
    
    // MARK: UITableViewDataSource
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return filteredParties.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "partyCell") as! PartyTableViewCell
        guard let party = filteredParties[safe: indexPath.row] else { return cell }
        cell.lblName.text = party.name
        cell.lblHost.text = "Hosted by: \(party.hostName)"
        cell.lblAttendees.text = "\(party.attendees < 1000 ? "\(party.attendees)" : ">999")"
        cell.backgroundColor = party.isHost ? .colorPrimaryDark : .colorBg
        cell.showLock = party.hasPassword
        if let song = party.queue.first {
            cell.imgAlbumArt.sd_setImage(with: URL(string: song.albumUrl))
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let party = self.filteredParties[safe: indexPath.row] else { return }
        if party.hasPassword {
            passwordPopUp.frame = self.view.frame
            passwordPopUp.successCallback = {
                 ControllerInterface.DoSegue(segueCommand: .ToParty, viewController: self, segueType: .Show, extraDataObject: party)
            }
            passwordPopUp.party = party
            view.addSubview(passwordPopUp)
        } else {
            ControllerInterface.DoSegue(segueCommand: .ToParty, viewController: self, segueType: .Show, extraDataObject: party)
        }
    }
    var filterString = ""
    // MARK: AppBarViewDelegate
    func appBarSearchDidChange(searchText: String) {
        filterString = searchText
        displayParties()
    }
    
    func displayParties() {
        if !filterString.isEmpty {
            filteredParties = parties.filter {
                $0.name.lowercased().contains(filterString.lowercased())
            }
        } else {
            filteredParties = parties
        }
        DispatchQueue.main.async {
            self.tableView.reloadData()
        }
    }
}
